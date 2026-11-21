(ns web.widgets.folder.events
  "Files of a folder of a storage service: the files of one thing (a room,
   a business profile) under the application it belongs to.

   A folder is {:service :folder {:kind :id} :spec :application-id :call-fx
   :put-fx}: :spec names the entries it takes, :application-id is left out
   when the call effect adds it, :call-fx is the effect the application sends
   backend requests with (default edd.client's), taking {:service :query
   | :commands :on-success :on-failure}, :put-fx the one sending the bytes
   (default ::put).

   An upload is always a new file: what a thing shows changes only when the
   thing is saved, and ::retain then lets the folder drop what it no longer
   uses. A cancelled edit leaves nothing behind once the next save is done.

   Saving while pictures upload would leave them out. The app's save waits:
   (rf/dispatch [::when-uploaded folder [:app/save]]) dispatches :app/save
   once no upload of the folder runs, after the fields got the URLs of the
   finished ones; [::subs/uploading folder] counts the running ones, to
   disable a save button or say what it waits for."
  (:require [edd.client :as client]
            [goog.crypt.base64 :as b64]
            [re-frame.core :as rf]
            [web.widgets.folder.db :as db]
            [web.widgets.folder.files :as files])
  (:import [goog.crypt Md5]))

(def default-refresh-ms
  "How often a private entry re-reads :folder->get to refresh its presigned
   urls. The storage service signs a GET url for download-expires-sec (3600 s
   = 1 h); 25 min is comfortably under half of that, so a url is replaced long
   before it expires. A prop :refresh-ms overrides it."
  (* 25 60 1000))

(def ^:private ref-poll-ms
  "Between polls of :folder->get while a private upload is becoming ready."
  2000)

(def ^:private ref-poll-max
  30)

(defn images-spec
  "Spec of a folder of pictures, every entry a list so that each upload is a
   file of its own. An entry is a keyword, or a map {:name keyword :crop crop
   :public bool}: :crop turns cropping on (true for a free crop, {:aspect
   ratio} for a fixed one); :public false makes the entry private, its files
   reachable only through a short-lived presigned url. Private entries are
   still pictures (image type and magic-byte check), only not served from the
   CDN. :public defaults to true."
  [entries]
  (mapv (fn [entry]
          (let [{:keys [name crop public]
                 :or {public true}}
                (if (map? entry) entry {:name entry})]
            (cond-> {:name name
                     :list true
                     :public public
                     :types files/image-types}
              crop (assoc :crop crop))))
        entries))

(defn entry-spec
  [{:keys [spec]} entry]
  (first (filter #(= entry (:name %)) spec)))

(defn private-entry?
  "A private entry has :public false in the folder spec; without a spec or a
   flag an entry is public, so the public path is the default."
  [props entry]
  (false? (:public (entry-spec props entry))))

(defn file-ref
  "The stable identity of a stored file: the path of its url, which is
   \"/\" followed by the storage key. A public url and a private presigned
   url both carry it, and it does not change when the signed url is
   refreshed, so a form field holds it (never the expiring signed url) and
   ::retain keeps the file by it."
  [url]
  (when url
    (.-pathname (js/URL. url))))

(defn signed-url
  "The current presigned url of the private file the stable ref names, read
   from the files of the last :folder->get; nil until the folder was read."
  [files ref]
  (some (fn [{:keys [url]}]
          (when (and url (= ref (file-ref url))) url))
        files))

(defn- call
  [{:keys [call-fx]} request]
  [(or call-fx ::client/call) request])

(defn- scoped
  [{:keys [application-id folder]} request]
  (cond-> (assoc request :folder folder)
    application-id (assoc :application-id application-id)))

(defn- command
  [props cmd-id attrs]
  (scoped props {:cmd-id cmd-id
                 :attrs attrs}))

(defn get-query
  "Reads the folder. The query id is :folder->get for the allora storage
   service; a service using another id overrides it with the :get-query-id
   prop."
  [props]
  (scoped props {:query-id (or (:get-query-id props) :folder->get)}))

(defn refresh-ms
  [props]
  (or (:refresh-ms props) default-refresh-ms))

(defn save-command
  "Creates the folder, or gives it the spec of props."
  [{:keys [spec] :as props}]
  (command props :folder->save {:spec spec}))

(defn retain-command
  [props urls]
  (command props :folder->retain {:urls (vec urls)}))

(defn- send
  [props commands {:keys [on-success on-failure]}]
  {:fx [(call props {:service (:service props)
                     :commands commands
                     :on-success (or on-success [::ignored])
                     :on-failure (or on-failure [::ignored])})]})

(defn save
  "Sets the folder up, sent when the thing it belongs to is created."
  [_cofx [_ props callbacks]]
  (send props [(save-command props)] callbacks))

(defn retain
  "After the thing was saved: the folder keeps the files of urls."
  [_cofx [_ props urls callbacks]]
  (send props [(save-command props) (retain-command props urls)] callbacks))

(defn running
  "Uploads of the folder of props still sending their file."
  [db props]
  (filter #(= :uploading (:status %))
          (vals (get-in db (db/uploads-path props)))))

(defn uploading?
  [db props]
  (boolean (seq (running db props))))

(defn upload
  "upload: {:entry :item :file :file-name :content-type :on-uploaded},
   on-uploaded a fn or an event getting the public URL of the file once it
   is stored. Uploads of an entry are handed on in the order they started."
  [{:keys [db]} [_ props {:keys [entry item ^js file] :as upload}]]
  (let [order
        (get-in db db/order-path 0)

        started
        (assoc upload
               :file-name (or (:file-name upload) (.-name file))
               :content-type (or (:content-type upload) (not-empty (.-type file)))
               :order order
               :status :uploading)

        attrs
        (cond-> {:entry entry
                 :item item
                 :file-name (:file-name started)}
          (:content-type started) (assoc :content-type (:content-type started)))

        counted
        (assoc-in db db/order-path (inc order))]
    {:db (assoc-in counted (db/upload-path props item) started)
     :fx [(call props {:service (:service props)
                       :commands [(save-command props)
                                  (command props :folder->add-file attrs)]
                       :on-success [::added props started]
                       :on-failure [::upload-failed props started]})]}))

(defn md5-base64
  "Content-MD5 value of bytes (an ArrayBuffer or Uint8Array)."
  [bytes]
  (let [md5
        (Md5.)]
    (.update md5 (js/Uint8Array. bytes))
    (b64/encodeByteArray (.digest md5))))

(defn digest!
  [{:keys [^js file on-success on-failure]}]
  (let [read
        (.then (.arrayBuffer file)
               (fn [bytes]
                 (rf/dispatch (conj on-success (md5-base64 bytes)))))]
    (.catch read
            (fn [error]
              (rf/dispatch (conj on-failure {:error (str error)}))))))

(defn added
  "The MD5 of the file is signed into its upload URL: storage buckets with
   Object Lock refuse a PUT without Content-MD5."
  [_cofx [_ props {:keys [file] :as upload}]]
  {:fx [[::digest {:file file
                   :on-success [::digested props upload]
                   :on-failure [::upload-failed props upload]}]]})

(defn digested
  [_cofx [_ props {:keys [entry item ^js file] :as upload} md5]]
  {:fx [(call props {:service (:service props)
                     :query (scoped props {:query-id :folder->upload-url
                                           :entry entry
                                           :item item
                                           :content-length (.-size file)
                                           :md5 md5})
                     :on-success [::url-received props (assoc upload :md5 md5)]
                     :on-failure [::upload-failed props upload]})]})

(defn url-received
  "A public file is handed on by its immutable :public-url once the bytes are
   stored. A private file has none: it reads the folder for the presigned url
   and hands on the stable ref instead."
  [_cofx [_ props {:keys [entry file content-type md5] :as upload} {:keys [result]}]]
  {:fx [[(or (:put-fx props) ::put) {:url (:url result)
                                     :headers (:headers result)
                                     :content-type content-type
                                     :md5 md5
                                     :file file
                                     :on-success (if (private-entry? props entry)
                                                   [::resolve-private props upload 0]
                                                   [::stored props upload (:public-url result)])
                                     :on-failure [::upload-failed props upload]}]]})

(defn- handed-on
  "Stored uploads of an entry no earlier running upload holds back."
  [uploads]
  (filter #(= :stored (:status %))
          (take-while #(not= :uploading (:status %)) uploads)))

(defn- hand-on
  "Hands the field the stored file's saved value: the public url of a public
   file, the stable ref of a private one. Never an expiring signed url."
  [{:keys [on-uploaded value]}]
  (cond
    (fn? on-uploaded)
    [::callback [on-uploaded value]]

    on-uploaded
    [:dispatch (conj on-uploaded value)]))

(defn- settled
  "After an upload ended: the stored ones go to their fields in the order
   they started, then the events waiting for the folder once none runs."
  [db props]
  (let [by-entry
        (vals (group-by :entry (sort-by :order (vals (get-in db (db/uploads-path props))))))

        done
        (mapcat handed-on by-entry)

        remaining
        (apply update-in db (db/uploads-path props) dissoc (map :item done))

        idle?
        (not (uploading? remaining props))

        waiting
        (when idle?
          (get-in remaining (db/waiting-path props)))]
    {:db (if idle?
           (assoc-in remaining (db/waiting-path props) [])
           remaining)
     :fx (into (vec (keep hand-on done))
               (map (fn [event] [:dispatch event]) waiting))}))

(defn stored
  "value is what the field keeps (public url or private stable ref);
   preview-url is what the tile shows while it is handed on (the signed url
   for a private file, the public url otherwise)."
  [{:keys [db]} [_ props {:keys [item preview-url]} value]]
  (if (get-in db (db/upload-path props item))
    (settled (update-in db (db/upload-path props item) assoc
                        :status :stored
                        :value value
                        :preview-url (or preview-url value))
             props)
    {:db db}))

(defn- ready-file
  [files {:keys [entry item]}]
  (first (filter #(and (= entry (:entry %))
                       (= item (:item %))
                       (:url %))
                 files)))

(defn resolve-private
  "A private file's bytes are stored; its presigned url and stable ref come
   from :folder->get once the storage service marks it ready. Polls until it
   is, then stores it like a public one but with the ref as the saved value."
  [_cofx [_ props upload attempt]]
  {:fx [(call props {:service (:service props)
                     :query (get-query props)
                     :on-success [::private-resolved props upload attempt]
                     :on-failure [::private-resolved props upload attempt]})]})

(defn private-resolved
  [{:keys [db]} [_ props {:keys [item] :as upload} attempt {:keys [result]}]]
  (let [files
        (:files result)

        ready
        (ready-file files upload)

        cached
        (assoc-in db (db/files-path props) (or files (get-in db (db/files-path props))))]
    (cond
      ready
      (stored {:db cached}
              [::stored props (assoc upload :preview-url (:url ready)) (file-ref (:url ready))])

      (< attempt ref-poll-max)
      {:db cached
       :fx [[:dispatch-later {:ms ref-poll-ms
                              :dispatch [::resolve-private props upload (inc attempt)]}]]}

      :else
      (settled (update-in cached (db/upload-path props item) assoc
                          :status :failed
                          :response {:error "not ready"})
               props))))

(defn upload-failed
  [{:keys [db]} [_ props {:keys [item]} response]]
  (if (get-in db (db/upload-path props item))
    (settled (update-in db (db/upload-path props item) assoc
                        :status :failed
                        :response response)
             props)
    {:db db}))

(defn dismiss
  [db [_ props item]]
  (update-in db (db/uploads-path props) dissoc item))

(defn when-uploaded
  "Dispatches event once no upload of the folder of props runs."
  [{:keys [db]} [_ props event]]
  (if (uploading? db props)
    {:db (update-in db (db/waiting-path props) (fnil conj []) event)}
    {:fx [[:dispatch event]]}))

(defn fetch
  "Reads the folder and caches its files, so a private entry can resolve the
   presigned url of each stored file."
  [_cofx [_ props]]
  {:fx [(call props {:service (:service props)
                     :query (get-query props)
                     :on-success [::fetched props]
                     :on-failure [::ignored]})]})

(defn fetched
  [{:keys [db]} [_ props {:keys [result]}]]
  {:db (assoc-in db (db/files-path props) (:files result))})

(defn watch
  "On mount of an entry: a private entry reads the folder for its presigned
   urls and keeps them fresh until unmount; a public entry reads nothing, so
   its immutable CDN urls never trigger a query or a timer."
  [{:keys [db]} [_ props entry]]
  (if (private-entry? props entry)
    {:db (assoc-in db (db/watch-path props entry) true)
     :fx [[:dispatch [::fetch props]]
          [:dispatch-later {:ms (refresh-ms props)
                            :dispatch [::refresh props entry]}]]}
    {}))

(defn refresh
  "Re-reads the folder and schedules the next refresh, while the entry is
   still watched (mounted)."
  [{:keys [db]} [_ props entry]]
  (when (get-in db (db/watch-path props entry))
    {:fx [[:dispatch [::fetch props]]
          [:dispatch-later {:ms (refresh-ms props)
                            :dispatch [::refresh props entry]}]]}))

(defn unwatch
  [db [_ props entry]]
  (update-in db (vec (butlast (db/watch-path props entry))) dissoc entry))

(defn put-headers
  "The headers the URL was signed for, with the Content-MD5 of the file when
   the storage service did not list it."
  [{:keys [headers content-type md5]}]
  (let [signed
        (or headers
            (when content-type
              {"Content-Type" content-type})
            {})

        listed?
        (some #(= "content-md5" (.toLowerCase (name %))) (keys signed))]
    (if (and md5 (not listed?))
      (assoc signed "Content-MD5" md5)
      signed)))

(defn put!
  "PUT of the bytes to a presigned URL with the headers it was signed for;
   the browser sets their length."
  [{:keys [url ^js file on-success on-failure] :as request}]
  (let [sent-headers
        (put-headers request)

        sending
        (js/fetch url #js {:method "PUT"
                           :body file
                           :headers (clj->js sent-headers)})

        answered
        (.then sending
               (fn [^js response]
                 (rf/dispatch (if (.-ok response)
                                on-success
                                (conj on-failure {:status (.-status response)})))))]
    (.catch answered
            (fn [error]
              (rf/dispatch (conj on-failure {:error (str error)}))))))

(defn run-callback
  [_cofx [_ f & args]]
  {:fx [[::callback (into [f] args)]]})

(rf/reg-fx ::put put!)
(rf/reg-fx ::digest digest!)
(rf/reg-fx ::callback (fn [[f & args]] (apply f args)))
(rf/reg-event-fx ::save save)
(rf/reg-event-fx ::retain retain)
(rf/reg-event-fx ::upload upload)
(rf/reg-event-fx ::added added)
(rf/reg-event-fx ::digested digested)
(rf/reg-event-fx ::url-received url-received)
(rf/reg-event-fx ::stored stored)
(rf/reg-event-fx ::resolve-private resolve-private)
(rf/reg-event-fx ::private-resolved private-resolved)
(rf/reg-event-fx ::upload-failed upload-failed)
(rf/reg-event-db ::dismiss dismiss)
(rf/reg-event-fx ::when-uploaded when-uploaded)
(rf/reg-event-fx ::fetch fetch)
(rf/reg-event-fx ::fetched fetched)
(rf/reg-event-fx ::watch watch)
(rf/reg-event-fx ::refresh refresh)
(rf/reg-event-db ::unwatch unwatch)
(rf/reg-event-fx ::run-callback run-callback)
(rf/reg-event-db ::ignored (fn [db _] db))
