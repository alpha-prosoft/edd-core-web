(ns web.widgets.folder.events
  "Files of a folder of a storage service: the files of one thing (a room,
   a business profile) under the application it belongs to.

   A folder is {:service :folder {:kind :id} :spec :application-id :call-fx}:
   :spec names the entries it takes, :application-id is left out when the
   call effect adds it, :call-fx is the effect the application sends
   backend requests with (default edd.client's), taking {:service :query
   | :commands :on-success :on-failure}.

   An upload is always a new file: what a thing shows changes only when the
   thing is saved, and ::retain then lets the folder drop what it no longer
   uses. A cancelled edit leaves nothing behind once the next save is done."
  (:require [edd.client :as client]
            [re-frame.core :as rf]
            [web.widgets.folder.db :as db]))

(defn images-spec
  "Spec of a folder of public pictures, every entry a list so that each
   upload is a file of its own."
  [entries]
  (mapv (fn [entry]
          {:name entry
           :list true
           :public true})
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

(defn upload
  "upload: {:entry :item :file :on-uploaded}, on-uploaded the event that
   gets the public URL of the file once it is stored."
  [{:keys [db]} [_ props {:keys [entry item ^js file] :as upload}]]
  (let [content-type
        (not-empty (.-type file))

        attrs
        (cond-> {:entry entry
                 :item item
                 :file-name (.-name file)}
          content-type (assoc :content-type content-type))]
    {:db (assoc-in db (db/upload-path props item) (assoc upload :status :uploading))
     :fx [(call props {:service (:service props)
                       :commands [(save-command props)
                                  (command props :folder->add-file attrs)]
                       :on-success [::added props upload]
                       :on-failure [::upload-failed props upload]})]}))

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
  [_cofx [_ props {:keys [file] :as upload} {:keys [result]}]]
  {:fx [[::put {:url (:url result)
                :file file
                :on-success [::stored props upload (:public-url result)]
                :on-failure [::upload-failed props upload]}]]})

(defn stored
  [{:keys [db]} [_ props {:keys [item on-uploaded]} public-url]]
  {:db (update-in db (db/uploads-path props) dissoc item)
   :fx [(when on-uploaded
          [:dispatch (conj on-uploaded public-url)])]})

(defn upload-failed
  [db [_ props {:keys [item]} response]]
  (update-in db (db/upload-path props item) assoc
             :status :failed
             :response response))

(defn dismiss
  [db [_ props item]]
  (update-in db (db/uploads-path props) dissoc item))

(defn put!
  "PUT of the bytes to a presigned URL; the browser sets their length."
  [{:keys [url ^js file on-success on-failure]}]
  (let [headers
        (if (not-empty (.-type file))
          #js {"Content-Type" (.-type file)}
          #js {})]
    (-> (js/fetch url #js {:method "PUT" :body file :headers headers})
        (.then (fn [^js response]
                 (rf/dispatch (if (.-ok response)
                                on-success
                                (conj on-failure {:status (.-status response)})))))
        (.catch (fn [error]
                  (rf/dispatch (conj on-failure {:error (str error)})))))))

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
(rf/reg-event-db ::upload-failed upload-failed)
(rf/reg-event-db ::dismiss dismiss)
(rf/reg-event-fx ::run-callback run-callback)
(rf/reg-event-db ::ignored (fn [db _] db))
