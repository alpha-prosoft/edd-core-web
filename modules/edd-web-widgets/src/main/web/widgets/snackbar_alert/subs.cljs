(ns web.widgets.snackbar-alert.subs
  (:require
    [re-frame.core :as rf]
    [web.widgets.snackbar-alert.db :as db]))

(rf/reg-sub
  ::message
  (fn [db]
      (get-in db [::db/message])))

(rf/reg-sub
  ::auto-hide-duration
  (fn [db]
      (get-in db [::db/auto-hide-duration])))

(rf/reg-sub
  ::severity
  (fn [db]
      (get-in db [::db/severity])))

(rf/reg-sub
  ::anchor-origin
  (fn [db]
    (get-in db [::db/anchor-origin])))