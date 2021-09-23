(ns projectname.home.events
  (:require
   [re-frame.core :as rf]
   [projectname.home.db :as db]))

(rf/reg-event-db
 :initialize-home-db
 (fn [db _]
   (merge db db/default-db)))

(rf/reg-event-db
 ::click
 (fn [db _]
   (update-in db [::db/clicks] inc)))
