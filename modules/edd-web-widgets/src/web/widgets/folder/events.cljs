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
            [re-frame.core :as rf]
            [web.widgets.folder.db :as db]
            [web.widgets.folder.files :as files]))

(defn images-spec
  "Spec of a folder of public pictures, every entry a list so that each
   upload is a file of its own."
  [entries]
  (mapv (fn [entry]
          {:name entry
           :list true
           :public true
           :types files/image-types})
        entries))

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

(defn added
  [_cofx [_ props {:keys [entry item ^js file] :as upload}]]
  {:fx [(call props {:service (:service props)
                     :query (scoped props {:query-id :folder->upload-url
                                           :entry entry
                                           :item item
                                           :content-length (.-size file)})
                     :on-success [::url-received props upload]
                     :on-failure [::upload-failed props upload]})]})

(defn url-received
  [_cofx [_ props {:keys [file content-type] :as upload} {:keys [result]}]]
  {:fx [[(or (:put-fx props) ::put) {:url (:url result)
                                     :headers (:headers result)
                                     :content-type content-type
                                     :file file
                                     :on-success [::stored props upload (:public-url result)]
                                     :on-failure [::upload-failed props upload]}]]})

(defn- handed-on
  "Stored uploads of an entry no earlier running upload holds back."
  [uploads]
  (filter #(= :stored (:status %))
          (take-while #(not= :uploading (:status %)) uploads)))

(defn- hand-on
  [{:keys [on-uploaded public-url]}]
  (cond
    (fn? on-uploaded)
    [::callback [on-uploaded public-url]]

    on-uploaded
    [:dispatch (conj on-uploaded public-url)]))

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
  [{:keys [db]} [_ props {:keys [item]} public-url]]
  (if (get-in db (db/upload-path props item))
    (settled (update-in db (db/upload-path props item) assoc
                        :status :stored
                        :public-url public-url)
             props)
    {:db db}))

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

(defn put!
  "PUT of the bytes to a presigned URL with the headers it was signed for;
   the browser sets their length."
  [{:keys [url headers content-type ^js file on-success on-failure]}]
  (let [sent-headers
        (or headers
            (when content-type
              {"Content-Type" content-type}))

        sending
        (js/fetch url #js {:method "PUT"
                           :body file
                           :headers (clj->js (or sent-headers {}))})

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
(rf/reg-fx ::callback (fn [[f & args]] (apply f args)))
(rf/reg-event-fx ::save save)
(rf/reg-event-fx ::retain retain)
(rf/reg-event-fx ::upload upload)
(rf/reg-event-fx ::added added)
(rf/reg-event-fx ::url-received url-received)
(rf/reg-event-fx ::stored stored)
(rf/reg-event-fx ::upload-failed upload-failed)
(rf/reg-event-db ::dismiss dismiss)
(rf/reg-event-fx ::when-uploaded when-uploaded)
(rf/reg-event-fx ::run-callback run-callback)
(rf/reg-event-db ::ignored (fn [db _] db))
