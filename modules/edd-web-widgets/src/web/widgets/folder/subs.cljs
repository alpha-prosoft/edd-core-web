(ns web.widgets.folder.subs
  (:require [re-frame.core :as rf]
            [web.widgets.folder.db :as db]
            [web.widgets.folder.events :as events]))

(defn uploads
  "Uploads of an entry running, failed or stored but held back by an earlier
   one, in the order the files were chosen."
  [db [_ props entry]]
  (vec (sort-by :order
                (filter #(= entry (:entry %))
                        (vals (get-in db (db/uploads-path props)))))))

(defn uploading
  "How many uploads of the folder are running."
  [db [_ props]]
  (count (events/running db props)))

(defn files
  "The files of the last :folder->get, for resolving a private entry's signed
   url."
  [db [_ props]]
  (get-in db (db/files-path props)))

(rf/reg-sub ::uploads uploads)
(rf/reg-sub ::uploading uploading)
(rf/reg-sub ::files files)
