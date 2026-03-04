(ns projectname.about.events
  (:require
   [re-frame.core :as re-frame]
   [projectname.about.db :as db]))

;; Use assoc instead of merge-with-defaults so re-init doesn't blow away page state
(re-frame/reg-event-db
 ::init
 (fn [db [_ params]]
   (assoc db ::db/params params)))

(re-frame/reg-event-db
 ::click
 (fn [db _]
   (update-in db [::db/clicks] inc)))
