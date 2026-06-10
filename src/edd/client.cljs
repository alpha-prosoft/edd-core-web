(ns edd.client
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [goog.object :as g]
   [malli.core :as m]
   [malli.error :as me]
   [edd.json :as json]
   [edd.db :as db]
   [clojure.string :as string]
   [edd.client-utils :as utils]))

(def timeout-rounding 0)

(deftype MockHeaders [headers]
  Object
  (get [_] nil))

(deftype MockResponse [body status headers]
  Object
  (json [_] body)
  (-status [_] status)
  (-headers [_] headers))

(defn mock-response [mock-func {:keys [query commands]}]
  (let [mock-result (mock-func (or query commands))
        log-mocks? (g/get js/window "log-mocks?")]
    (when log-mocks?
      (print mock-result))
    (MockResponse. (:body mock-result)
                   (:status mock-result)
                   (MockHeaders. {"versionid" "mock-response"}))))

(defn get-config []
  (-> (db/non-reactive-db) ::db/config))

(defn get-record-call-failure-func []
  (-> (db/non-reactive-db) ::db/record-call-failure-func))

(defn get-record-call-func []
  (-> (db/non-reactive-db) ::db/record-call-func))

(defn get-handle-expired-jwt-func []
  (-> (db/non-reactive-db) ::db/on-expired-jwt-func))

(defn get-proxy-or-host [host]
  (let [{:keys [proxies use-proxy-for]} (get-config)
        current-host (-> js/window .-location .-host)
        use-proxy? (contains? (set use-proxy-for) current-host)
        proxy-host (get proxies (keyword host))]
    (if (and use-proxy?
             (some? proxy-host))
      proxy-host
      host)))

(defn document-uri [service path]
  (let [hosted-zone-name (-> (get-config) :HostedZoneName)
        host (get-proxy-or-host (str (name service) "." hosted-zone-name))]
    (str "https://" host path)))

(defn stage-for-realm [realm]
  (let [client-routing (-> (get-config) :clientRouting)
        client-routing (update client-routing :default #(or % "prod"))]
    (if (some? realm)
      (or (-> client-routing :realms realm)
          (-> client-routing :default))
      (-> client-routing :default))))

(defn service-uri [service path]
  (let [hosted-zone-name (-> (get-config) :HostedZoneName)
        realm (-> (db/non-reactive-db) ::db/user :realm)
        stage (stage-for-realm realm)
        host (get-proxy-or-host (str "api." hosted-zone-name))]
    (str "https://"
         host
         "/private/"
         (str stage)
         "/"
         (name service) path)))

(defn assign-request-meta
  [req]
  (let [db
        (db/non-reactive-db)

        with-user
        (assoc req
               :user
               {:selected-role (get-in db [::db/user :selected-role])})

        with-meta
        (assoc with-user
               :meta
               (merge
                {:language (::db/selected-language db)}
                (::db/meta db)))]
    with-meta))

(defn make-headers
  []
  (let [db (db/non-reactive-db)]
    {"X-Authorization" (get-in db [::db/user :id-token])
     "Accept"          "*/*"
     "Content-Type"    "application/json"}))

(defn make-get-headers
  []
  (let [db (db/non-reactive-db)]
    {"X-Authorization" (get-in db [::db/user :id-token])
     "Accept"          "*/*"}))

(defn make-put-headers
  []
  (let [db (db/non-reactive-db)]
    {"X-Authorization" (get-in db [::db/user :id-token])
     "Accept"          "*/*"}))

(defn fetch
  [uri params]
  (.fetch js/window
          uri
          (clj->js params)))

(defn resolve-call-hook [response]
  (let [{:keys [item result]} response
        {:keys [on-success on-failure]} item]
    (if (and (some? response) (some? result))
      (do (when on-success
            (rf/dispatch (vec (concat on-success [response]))))
          true)
      (do (when on-failure
            (rf/dispatch (vec (concat on-failure [response]))))
          false))))

(defn resolve-calls-hooks [event-body]
  (doall
   (mapv
    (fn [it]
      (let [{:keys [item result]} it
            {:keys [on-success on-failure]} item]
        (if (and
             (some some? event-body)
             result)
          (do (when on-success
                (rf/dispatch (vec (concat on-success [it]))))
              true)
          (do (when on-failure
                (rf/dispatch (vec (concat on-failure [it]))))
              false))))
    event-body)))

(defn handle-invalid-jwt []
  (print "invalid token")
  (rf/dispatch [:edd.events-remove-user]))

(defn jwt-expired? [{:keys [status]}]
  (= status 401))

(defn handle-versioning-error [call]
  (-> call
      (assoc :error :wrong-version)
      (dissoc :body)))

(defn handle-error [call itm]
  (-> call
      (assoc :error (json/parse-custom-fields (:error itm)))
      (dissoc :body)))

(defn map-response-body [response]
  (cond
    (= ":invalid"
       (get-in response [:error :jwt]))
    (handle-invalid-jwt)

    (= "Wrong version"
       (:error response))
    (handle-versioning-error response)

    (contains? response :error)
    (json/parse-custom-fields response)

    (contains? response :exception)
    response

    :else
    {:result (json/parse-custom-fields (:result response))}))

(defn filter-results [values response-filter items bodies]
  (vec
   (map-indexed
    (fn [idx itm]
      (cond
        (= ":invalid" (get-in itm [:error :jwt])) (handle-invalid-jwt)
        (= "Wrong version" (:error itm)) (handle-versioning-error (get values idx))
        (contains? itm :error) (handle-error (get values idx) itm)
        :else (-> (get values idx)
                  (assoc :result (:result itm))
                  (#(if response-filter
                      (response-filter %)
                      %))
                  (assoc :item (get items idx))
                  (dissoc :body))))
    (js->clj bodies :keywordize-keys true))))

(defn calculate-default-timeout [total-attempts attempts-left]
  (let [attempt (- total-attempts attempts-left)]
    (* attempt attempt 3000)))

(defn handle-exception [{:keys [body status] :as r} attempt]
  (let [event-body (map-response-body body)
        throw-exception? (and
                          (not (neg? attempt))
                          (not (contains? event-body :error))
                          (or (< 499 status)
                              (contains? event-body :ecxeption)))]
    (if throw-exception?
      (throw (js/Error. r))
      r)))

(defn handle-response-and-return-succeed? [{:keys [body] :as r} on-success on-failure]
  (let [event-body (map-response-body body)]
    (if (some? (:result event-body))
      (do
        (rf/dispatch (conj on-success event-body))
        true)
      (do
        (rf/dispatch (conj on-failure event-body))
        false))))

(defn handle-save-response-and-return-succeed? [{:keys [body]} on-success on-failure]
  (let [{:keys [result]} (map-response-body body)]
    (if (some? result)
      (do
        (when on-success
          (rf/dispatch (conj on-success result)))
        (:result body))
      (do
        (rf/dispatch (conj on-failure result))
        false))))

(defn post-params [body-str]
  (merge
   {:method           :post
    :mode             :cors
    :body             (.stringify js/JSON body-str)
    :timeout          31000
    :response-format  (json/custom-response-format {:keywords? true})
    :headers          (make-headers)}
   (-> (get-config) :post-params)))

(defn put-params [data]
  (merge
   {:mode             "cors"
    :method           "PUT"
    :headers          (make-put-headers)
    :body             data}
   (-> (get-config) :put-params)))

(defn get-params []
  (merge
   {:method           :get
    :timeout          50000
    :response-format  (json/custom-response-format {:keywords? true})
    :headers          (make-get-headers)}
   (-> (get-config) :get-params)))

(defn get-mock-func [{:keys [query commands service]} mock-for]
  (let [mock-func-name (case mock-for
                         :query (str "mock." (name service) "." (name (:query-id query)))
                         :commands (str "mock." (name service) "." (reduce str (map #(name (:cmd-id %)) commands))))]
    (g/get js/window mock-func-name)))

(defn get-uri [{:keys [query commands service]} mock-for]
  (case mock-for
    :query (service-uri service (str "/query"
                                     "?dbg_service=" service
                                     "&dbg_qid=" (:query-id query)))
    :commands (service-uri service (str "/command"
                                        "?dbg_service=" service
                                        "&dbg_cmds=" (string/join "," (map :cmd-id commands))))))

(defn convert-keyword->js [key-word]
  (if-let [keyword-namespace (namespace key-word)]
    (str keyword-namespace "/" (name key-word))
    (name key-word)))

(defn get-body-str [{:keys [query commands]} mock-for]
  (let [ref (case mock-for
              :query {:request-id     (random-uuid)
                      :interaction-id utils/interaction-id
                      :query          query}
              :commands {:request-id     (random-uuid)
                         :interaction-id utils/interaction-id
                         :commands       commands})]
    (clj->js (json/encode-custom-fields (assign-request-meta ref))
             {:keyword-fn convert-keyword->js})))

(defn do-post-with-retry
  ([post-for props retry-attempts]
   (let [body-str (get-body-str props post-for)]
     (do-post-with-retry post-for props retry-attempts body-str)))

  ([post-for {:keys [on-success on-failure retry service] :as props} retry-attempts body-str]
   (let [{:keys [timeout on-retry attempts] :or {attempts 2}} retry
         mock-func (get-mock-func props post-for)
         uri (get-uri props post-for)
         attempt (dec retry-attempts)
         record-call-failure-func (get-record-call-failure-func)
         record-call-func (get-record-call-func)
         start-time (system-time)]
     (->
      (js/Promise.resolve
       (clj->js
        (if (some? mock-func)
          (mock-response mock-func props)
          (fetch uri (post-params body-str)))))
      (.then (fn [r] (-> (js/Promise.resolve r)
                         (.then (fn [r] {:status   (.-status r)
                                         :response r})))))
      (.then (fn [r] (-> (js/Promise.resolve (-> r :response .json))
                         (.then (fn [body] (merge r {:body (js->clj body :keywordize-keys true)}))))))
      (.then (fn [r] (let [expired? (jwt-expired? r)
                           handle-expired-jwt-func (get-handle-expired-jwt-func)]
                       (if (and expired?
                                (some? handle-expired-jwt-func))
                         (handle-expired-jwt-func {:do-post-with-retry do-post-with-retry
                                                   :post-for           post-for
                                                   :props              post-for
                                                   :attempt            attempt
                                                   :body-str           body-str})
                         r))))
      (.then (fn [r] (do
                       (when (some? record-call-func)
                         (record-call-func
                          (name post-for)
                          {:took     (.toFixed (- (system-time) start-time) timeout-rounding)
                           :request  body-str
                           :service service
                           :response (select-keys (:body r)
                                                  [:invocation-id :request-id :interaction-id])}))
                       (handle-exception r attempt))))
      (.then (fn [r] (handle-response-and-return-succeed? r on-success on-failure)))
      (.catch (fn [e] (let [timeout (or timeout
                                        (calculate-default-timeout attempts attempt))]
                        (if (neg? attempt)
                          (do
                            (when (some? record-call-failure-func)
                              (record-call-failure-func
                               (.toString e)
                               {:request body-str
                                :service service}))
                            (rf/dispatch (conj on-failure (.toString e)))
                            false)
                          (do
                            (when (some? on-retry)
                              (rf/dispatch on-retry))
                            (js/setTimeout
                             (fn [] (do-post-with-retry post-for props attempt body-str))
                             timeout))))))))))

(defn do-post-for [post-for {:keys [retry] :as props}]
  (let [{:keys [attempts] :or {attempts 2}} retry]
    (do-post-with-retry post-for props attempts)))

(defn call [data]
  (cond
    (:query data) (do-post-for :query data)
    (:commands data) (do-post-for :commands data)
    (:command data) (do-post-for :commands (assoc data
                                                  :commands
                                                  [(:command data)]))
    :else nil))

(rf/reg-event-fx
 ::save-success
 (fn [_ [_ on-success result]]
   {:dispatch (conj on-success {:version-id (:version-id result)
                                :id         (get-in result [:body :result :id])})}))

(rf/reg-event-fx
 ::save-failure
 (fn [_ [_ on-failure]]
   (println on-failure)
   {:dispatch [on-failure]}))

(defn load-with-retry [uri
                       {:keys [on-success on-failure retry]
                        :as   props}
                       retry-attempts]
  (let [{:keys [timeout on-retry attempts] :or {attempts 2}} retry
        record-call-func (get-record-call-func)
        start-time (system-time)]
    (-> (js/Promise.resolve
         (clj->js
          (fetch uri (get-params))))
        (.then (fn [r] (.text r)))
        (.then (fn [result]
                 (do
                   (when (some? record-call-func)
                     (record-call-func
                      "load"
                      {:uri      uri
                       :took     (.toFixed (- (system-time) start-time) timeout-rounding)
                       :function "load"}))
                   (rf/dispatch (vec (concat on-success [result]))))))
        (.catch (fn [e] (let [attempt (dec retry-attempts)
                              timeout (or timeout
                                          (calculate-default-timeout attempts attempt))]
                          (if (neg? attempt)
                            (rf/dispatch (vec (concat on-failure [e])))
                            (do
                              (when (some? on-retry)
                                (rf/dispatch on-retry))
                              (js/setTimeout
                               (fn [] (load-with-retry uri props attempt))
                               timeout)))))))))

(defn load [{:keys [ref service retry] :as props}]
  (let [uri (document-uri :glms-content-svc (str "/load/" (name service) "/" ref))
        {:keys [attempts] :or {attempts 2}} retry]
    (load-with-retry uri props attempts)))

(rf/reg-fx
 :load
 (fn [{:keys [ref service on-success] :as props}]
   (let [mock-func-name (str "mock.load." (name service))
         mock-func (g/get js/window mock-func-name)]
     (if (some? mock-func)
       (rf/dispatch (vec (concat on-success [(mock-func ref)])))
       (load props)))))

(defn save-content-with-retry [uri
                               {:keys [data on-success on-failure retry]
                                :as   props}
                               retry-attempts]
  (let [{:keys [timeout on-retry attempts] :or {attempts 2}} retry
        record-call-func (get-record-call-func)
        start-time (system-time)]
    (->
     (js/Promise.resolve
      (clj->js
       (fetch uri (assoc-in (put-params data)
                            [:headers "Content-Type"]
                            "application/octet-stream"))))
     (.then (fn [r] (-> (js/Promise.resolve r)
                        (.then (fn [r] {:version-id (-> r
                                                        (.-headers)
                                                        (.get "versionid"))
                                        :status     (.-status r)
                                        :response   r})))))
     (.then (fn [r] (-> (js/Promise.resolve (-> r :response .json))
                        (.then (fn [body]
                                 (let [{:keys [version-id]} r
                                       body (js->clj body :keywordize-keys true)]
                                   {:body (assoc-in body [:result :version-id] version-id)}))))))
     (.then (fn [r]
              (do
                (when (some? record-call-func)
                  (record-call-func
                   "save"
                   {:uri      uri
                    :took     (.toFixed (- (system-time) start-time) timeout-rounding)
                    :function "save-content"}))
                (handle-save-response-and-return-succeed? r on-success on-failure))))
     (.catch (fn [e] (let [attempt (dec retry-attempts)
                           timeout (or timeout
                                       (calculate-default-timeout attempts attempt))]
                       (if (neg? attempt)
                         (do
                           (rf/dispatch (conj on-failure (.toString e)))
                           false)
                         (do
                           (when (some? on-retry)
                             (rf/dispatch on-retry))
                           (js/setTimeout
                            (fn [] (save-content-with-retry uri props attempt))
                            timeout)))))))))

(defn save-content
  [{:keys [data service retry] :as props}]
  (let [{:keys [attempts] :or {attempts 2}} retry
        uri (document-uri :glms-content-svc (str "/save/" (name service) "/" (random-uuid)))
        mock-func-name (str "mock.save." (name service))
        mock-func (g/get js/window mock-func-name)]
    (if (some? mock-func)
      (mock-response mock-func data)
      (save-content-with-retry uri props attempts))))

(defn handle-responses
  [items
   responses & {:keys [on-success on-failure response-filter]}]
  (let [values (utils/map-responses responses)]
    (if-not (utils/failed? values)
      (-> (js/Promise.all
           (clj->js
            (map
             #(:body %)
             values)))
          (.then (fn [bodies]
                   (let [event-body (filter-results values response-filter items bodies)
                         successes (resolve-calls-hooks event-body)]
                     (if (some false? successes)
                       (rf/dispatch (vec (concat on-failure event-body)))
                       (rf/dispatch (vec (concat on-success event-body))))))))
      (rf/dispatch on-failure))))

(defn save-n
  [items & {:keys [on-success on-failure]}]
  (let [requests (map save-content items)]
    (-> (js/Promise.all requests)
        (.then #(js->clj %))
        (.then (fn [items]
                 (let [with-failures (filter nil? items)
                       succeeded? (not
                                   (seq with-failures))]
                   (cond
                     (and on-failure
                          (not succeeded?)) (rf/dispatch on-failure)
                     (and on-success
                          succeeded?) (rf/dispatch (-> on-success
                                                       (conj items)
                                                       vec))))))
        (.catch #(rf/dispatch (vec (concat on-failure [%])))))))

(rf/reg-fx
 :save-n
 (fn [{:keys [items on-success on-failure]}]
   (save-n items
           :on-success on-success
           :on-failure on-failure)))

(rf/reg-fx
 :save
 (fn [{:keys [data service on-success on-failure]}]
   (save-n [{:data    data
             :service service}]
           :on-success on-success
           :on-failure on-failure)))

(defn call-n
  [{:keys [items on-success on-failure]}]
  (let [requests (map call (filterv identity items))]
    (-> (js/Promise.all requests)
        (.then #(js->clj %))
        (.then (fn [items]
                 (let [with-failures (filter #(not (true? %)) items)
                       succeeded? (empty? with-failures)]
                   (cond
                     (and (some? on-failure) (not succeeded?)) (rf/dispatch on-failure)
                     (and (some? on-success) succeeded?) (rf/dispatch on-success)))))
        (.catch #(rf/dispatch (vec (concat on-failure [%])))))))

(rf/reg-fx :call-n
           (fn [& args]
             (.warn js/console "DEPRICATED: use namespaced call-n (:require [edd.client :as client]) ::client/call-n")
             (apply call-n args)))
(rf/reg-fx ::call-n call-n)

(rf/reg-fx :call
           (fn [data]
             (.warn js/console "DEPRICATED: use namespaced call-n (:require [edd.client :as client]) ::client/call")
             (call-n {:items [data]})))
(rf/reg-fx ::call (fn [data] (call-n {:items [data]})))

(defonce timeouts (r/atom {}))

(rf/reg-fx
 :timeout
 (fn [{:keys [id event time]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))
   (when (some? event)
     (swap! timeouts assoc id
            (js/setTimeout
             (fn []
               (comp
                (swap! timeouts dissoc id)
                (rf/dispatch event)))
             time)))))

(rf/reg-fx
 :cancel-timeout
 (fn [{:keys [id]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))))

(defn after-timeout [id event]
  (if
   (get @timeouts id)
    (js/setTimeout
     (fn [] (after-timeout id event))
     100)
    (rf/dispatch event)))

(rf/reg-fx
 :after-timeout
 (fn [{:keys [id event]}]
   (after-timeout id event)))

(def DepSpecSchema
  [:map
   [:service keyword?]
   [:query [:or fn? map?]]
   [:depends-on {:optional true} [:vector keyword?]]
   [:retry {:optional true} [:map]]])

(def DepsSchema
  [:map-of keyword? DepSpecSchema])

(def DepsEffectSchema
  [:map
   [:deps DepsSchema]
   [:on-success {:optional true} vector?]
   [:on-failure {:optional true} vector?]])

(defn- explain-deps! [schema value label]
  (when-not (m/validate schema value)
    (let [explanation
          (m/explain schema value)

          humanized
          (me/humanize explanation)]
      (throw (js/Error. (str label " " (pr-str humanized)))))))

(defn- validate-depends-on! [deps]
  (let [all-keys
        (set (keys deps))]
    (doseq [[k {:keys [depends-on]}] deps
            :let [missing (vec (remove all-keys depends-on))]
            :when (seq missing)]
      (throw (js/Error.
              (str ":depends-on for " k " references unknown deps: " missing))))))

(defn topo-batches [deps]
  (loop [remaining deps
         resolved  #{}
         batches   []]
    (if (empty? remaining)
      batches
      (let [ready
            (into {}
                  (filter (fn [[_ {:keys [depends-on]}]]
                            (every? resolved (or depends-on []))))
                  remaining)

            ready-keys
            (set (keys ready))]
        (when (empty? ready-keys)
          (throw (ex-info "Circular dependency in :deps"
                          {:remaining (vec (keys remaining))})))
        (recur (apply dissoc remaining ready-keys)
               (into resolved ready-keys)
               (conj batches ready))))))

(defn- fetch-query [{:keys [service] :as props}]
  (let [body-str
        (get-body-str props :query)

        uri
        (get-uri props :query)

        record!
        (get-record-call-func)

        record-err!
        (get-record-call-failure-func)

        start-time
        (system-time)]
    (-> (js/Promise.resolve (fetch uri (post-params body-str)))
        (.then (fn [r]
                 (-> (.json r)
                     (.then (fn [body]
                              {:status (.-status r)
                               :body   (js->clj body :keywordize-keys true)})))))
        (.then (fn [{:keys [body]}]
                 (when record!
                   (record! "query"
                            {:took     (.toFixed (- (system-time) start-time) timeout-rounding)
                             :request  body-str
                             :service  service
                             :response (select-keys body [:invocation-id :request-id :interaction-id])}))
                 (let [parsed (map-response-body body)]
                   (if (some? (:result parsed))
                     parsed
                     (throw (ex-info "Query failed" {:response parsed}))))))
        (.catch (fn [e]
                  (when record-err!
                    (record-err! (.toString e) {:request body-str :service service}))
                  (throw e))))))

(defn- resolve-single-dep [resolved [k {:keys [query] :as spec}]]
  (let [query-map
        (if (fn? query) (query resolved) query)

        call-spec
        (-> spec
            (assoc :query query-map)
            (dissoc :depends-on))]
    (.then (fetch-query call-spec)
           (fn [body] [k (:result body)]))))

(defn- resolve-batches [deps]
  (let [batches
        (topo-batches deps)]
    (reduce
     (fn [p batch]
       (.then p
              (fn [acc]
                (-> (js/Promise.all
                     (mapv #(resolve-single-dep acc %) batch))
                    (.then (fn [pairs]
                             (reduce (fn [c [k v]] (assoc c k v))
                                     acc
                                     pairs)))))))
     (js/Promise.resolve {})
     batches)))

(rf/reg-fx
 ::deps
 (fn [{:keys [deps on-success on-failure] :as effect}]
   (explain-deps! DepsEffectSchema effect "Invalid ::deps effect:")
   (validate-depends-on! deps)
   (-> (resolve-batches deps)
       (.then (fn [resolved]
                (when on-success
                  (rf/dispatch (conj on-success resolved)))))
       (.catch (fn [err]
                 (when on-failure
                   (rf/dispatch (conj on-failure err))))))))
