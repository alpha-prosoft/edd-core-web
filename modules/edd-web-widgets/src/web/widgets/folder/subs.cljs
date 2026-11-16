(ns web.widgets.folder.subs
  (:require [re-frame.core :as rf]
            [web.widgets.folder.db :as db]))

(defn uploads
  "Uploads of an entry under way or failed, in the order they started."
  [db [_ props entry]]
  (vec (sort-by :started
                (filter #(= entry (:entry %))
                        (vals (get-in db (db/uploads-path props)))))))

(rf/reg-sub ::uploads uploads)
