(ns edd.events
  (:require
   [re-frame.core :as rf]
   [reitit.frontend :as reitit]
   [malli.core :as m]
   [malli.transform :as mt]
   [edd.client :as client]
   [edd.db :as db]))

(defn parse-query-string
  [search]
  (if (or (nil? search) (= "" search))
    {}
    (let [params
          (js/URLSearchParams. search)]
      (reduce (fn [m [k v]] (assoc m (keyword k) v))
              {}
              (es6-iterator-seq (.entries params))))))

(defn serialize-query-string
  [params]
  (let [sp
        (js/URLSearchParams.)]
    (doseq [[k v] params
            :when (some? v)]
      (.append sp (name k) (str v)))
    (let [s
          (.toString sp)]
      (if (= "" s) "" (str "?" s)))))

(defn split-url
  [url]
  (let [idx
        (.indexOf url "?")]
    (if (neg? idx)
      [url ""]
      [(.substring url 0 idx) (.substring url idx)])))

(defn decode-url-params
  [schema raw-params]
  (if (nil? schema)
    {:params raw-params}
    (let [decoded
          (m/decode schema raw-params (mt/string-transformer))]
      (if (m/validate schema decoded)
        {:params decoded}
        {:error (str "Invalid URL parameters: "
                     (pr-str (m/explain schema decoded)))}))))

(defn encode-url-params
  [schema params]
  (if (nil? schema)
    params
    (m/encode schema params (mt/string-transformer))))

(rf/reg-event-fx
 ::application-loaded
 (fn [{:keys [db]} [_ do-after-load {:keys [result]}]]
   {:db (-> db
            (assoc ::db/application result)
            (assoc-in [::db/config :ApplicationId]
                      (:id result))
            (assoc ::db/ready true))
    :fx [(when (some? do-after-load)
           (conj [:dispatch] do-after-load))]}))

(rf/reg-event-fx
 ::load-application
 (fn [{:keys [db]} [_ do-after-load]]
   (let [config (::db/config db)
         application-name (get config :ApplicationName)
         application-id (get config :ApplicationId)]
     (.info js/console (str "App name: " application-name))
     {:db db
      :fx [[::client/call {:on-success [::application-loaded do-after-load]
                           :service    (get config :ApplicationServiceName)
                           :query      (cond

                                         application-name
                                         {:query-id :application->fetch-by-name
                                          :name (get config :ApplicationName)}

                                         application-id
                                         {:query-id :application->fetch-by-id
                                          :id (get config :ApplicationId)})}]]})))

(rf/reg-event-fx
 ::initialize-db
 (fn [{:keys [db]} [_ {:keys [selected-language
                              show-language-switcher?
                              config
                              routes
                              pages-init-events
                              pages-url-params
                              error-pages
                              translations
                              record-call-failure-func
                              record-call-func
                              on-expired-jwt-func]
                       :or   {selected-language       :en
                              show-language-switcher? false}}]]

   (if (get db ::db/ready)
     {:db db}
     (let [application-name
           (get config :ApplicationName)

           db
           (let [with-defaults
                 (merge db/default-db db)

                 with-language
                 (assoc with-defaults ::db/selected-language selected-language)

                 with-switcher
                 (assoc with-language ::db/show-language-switcher? show-language-switcher?)

                 with-config
                 (assoc with-switcher ::db/config config)

                 with-init-events
                 (assoc with-config ::db/pages-init-events pages-init-events)

                 with-url-params
                 (assoc with-init-events ::db/pages-url-params (or pages-url-params {}))

                 with-error-pages
                 (assoc with-url-params ::db/error-pages (or error-pages {}))

                 with-routes
                 (assoc with-error-pages ::db/routes (reitit/router routes))

                 with-translations
                 (assoc with-routes ::db/translations translations)

                 with-failure-func
                 (assoc with-translations ::db/record-call-failure-func record-call-failure-func)

                 with-call-func
                 (assoc with-failure-func ::db/record-call-func record-call-func)

                 with-jwt-func
                 (assoc with-call-func ::db/on-expired-jwt-func on-expired-jwt-func)]
             with-jwt-func)

           current-url
           (str (.-pathname (.-location js/window))
                (.-search (.-location js/window)))]
       {:db (cond-> db

              (and (::db/user db)
                   application-name)
              (assoc ::db/ready false)

              (not (::db/user db))
              (assoc ::db/ready true))

        :fx [(if (and (::db/user db)
                      application-name)
               [:dispatch [::load-application [::navigate current-url]]]
               [:dispatch [::navigate current-url]])
             [::init-popstate nil]]}))))

(rf/reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ page & [params]]]
   {:db       (assoc db ::db/active-panel page
                     ::db/drawer false)
    :dispatch [(keyword (str "initialize-" (name page) "-db"))
               params]}))

(rf/reg-event-db
 ::toggle-drawer
 (fn [db _]
   (update db ::db/drawer #(not %))))

(rf/reg-event-db
 ::change-language
 (fn [db [_ value]]
   (assoc db ::db/selected-language value)))

(rf/reg-event-db
 :menu-toggle
 (fn [db event]
   (update-in db [::db/menu-expanded (second event)] #(not %))))

(rf/reg-event-db
 ::add-translation
 (fn [db [_ body]]
   (update-in db [::db/translations] #(merge % body))))

(rf/reg-event-fx
 ::navigate
 (fn [{:keys [db]} [_ target & rest-args]]
   (let [router
         (::db/routes db)

         pages-init
         (::db/pages-init-events db)

         pages-schemas
         (::db/pages-url-params db)

         {:keys [target-page url path-params query-params replace?]}
         (cond
           (and (map? target) (or (:page target) (:url target)))
           (if (:page target)
             {:target-page  (:page target)
              :path-params  (or (:path target) {})
              :query-params (or (:query target) {})
              :replace?     (:replace? target)}
             {:url      (:url target)
              :replace? (:replace? target)})

           (keyword? target)
           (let [[a b]
                 rest-args]
             {:target-page  target
              :path-params  (if (map? a) a {})
              :query-params (if (map? b) b {})})

           (string? target)
           {:url target})

         [handler path-params query-params pathname]
         (if target-page
           (let [match
                 (reitit/match-by-name router target-page path-params)]
             [target-page path-params query-params (:path match)])
           (let [[pathname search]
                 (split-url url)

                 match
                 (reitit/match-by-path router pathname)]
             [(some-> match :data :name keyword)
              (or (:path-params match) {})
              (parse-query-string search)
              pathname]))

         schema
         (get pages-schemas handler)

         {:keys [params error]}
         (when handler
           (decode-url-params schema query-params))

         push-or-replace
         (if replace?
           #(.replaceState (.-history js/window) #js {} "" %)
           #(.pushState (.-history js/window) #js {} "" %))]

     (cond
       (nil? handler)
       {:db (assoc db
                   ::db/active-panel :edd/not-found
                   ::db/error {:code 404 :detail (or url pathname)}
                   ::db/url-params {}
                   ::db/path-params {})}

       (some? error)
       (do (.warn js/console error)
           {:db (assoc db
                       ::db/active-panel :edd/bad-request
                       ::db/error {:code 400 :detail error}
                       ::db/url-params {}
                       ::db/path-params {})})

       :else
       (let [encoded
             (encode-url-params schema params)

             clean-qs
             (serialize-query-string
              (into {} (filter (comp some? val)) encoded))

             final-url
             (str pathname clean-qs)

             all-params
             (merge path-params params)]
         (push-or-replace final-url)
         {:db (assoc db
                     ::db/drawer false
                     ::db/url final-url
                     ::db/active-panel handler
                     ::db/url-params (or params {})
                     ::db/path-params path-params
                     ::db/error nil)
          :fx [[:dispatch [(get pages-init handler) all-params]]]})))))

(defonce ^:private popstate-initialized? (atom false))

(rf/reg-fx
 ::init-popstate
 (fn [_]
   (when-not @popstate-initialized?
     (reset! popstate-initialized? true)
     (.addEventListener js/window "popstate"
                        (fn [_]
                          (let [current-url
                                (str (.-pathname (.-location js/window))
                                     (.-search (.-location js/window)))]
                            (rf/dispatch [::navigate
                                          {:url      current-url
                                           :replace? true}])))))))

(rf/reg-event-db
 ::register-menu-item
 (fn [db [_ {:keys [key] :as item}]]
   (assoc db [::db/menu key] item)))

(rf/reg-event-db
 ::remove-user
 (fn [db]
   (assoc-in db [::db/user] nil)))

(rf/reg-event-db
 :edd.events-remove-user
 (fn [db]
   (assoc-in db [::db/user] nil)))

(rf/reg-event-db
 ::activate-request-feature
 (fn [db [_ key value]]
   (assoc-in db [::db/meta :request-features key] value)))

(rf/reg-event-db
 ::deactivate-request-feature
 (fn [db [_ key]]
   (if (some? key)
     (update-in db [::db/meta :request-features]  #(dissoc % key))
     (assoc-in db [::db/meta :request-features] {}))))
