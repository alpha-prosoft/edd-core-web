(ns projectname.home.events
  (:require
   [re-frame.core :as rf]
   [projectname.home.db :as db]
   [edd.events :as edd-events]))

(rf/reg-event-db
 :initialize-home-db
 (fn [db _]
   (merge db db/default-db)))

(rf/reg-event-db
 ::click
 (fn [db _]
   (update-in db [::db/clicks] inc)))

(rf/reg-event-fx
 ::open
 (fn [{:keys [db]} [_ id]]
   {:fx [[:dispatch [::edd-events/navigate :about {:id id}]]]}))
