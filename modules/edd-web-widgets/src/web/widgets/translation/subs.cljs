(ns web.widgets.translation.subs
  (:require
   [re-frame.core :as rf]
   [web.widgets.translation.db :as db]))

(rf/reg-sub
 ::initialized?
 (fn [db]
   (get-in db [::db/initialized?] false)))

(rf/reg-sub
 ::lang-list
 (fn [db]
   (get-in db [::db/lang-list])))

(rf/reg-sub
 ::selected-lang
 (fn [db]
   (get-in db [::db/selected-lang])))