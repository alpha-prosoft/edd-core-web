(ns web.widgets.confirmation-dialog.subs
  (:require
    [re-frame.core :as rf]
    [web.widgets.confirmation-dialog.db :as db]))

(rf/reg-sub
  ::dialog-message
  (fn [db]
    (get-in db [::db/dialog-message])))

(rf/reg-sub
  ::dialog-proceed-text
  (fn [db]
    (get-in db [::db/dialog-proceed-text])))

(rf/reg-sub
  ::dialog-cancel-text
  (fn [db]
    (get-in db [::db/dialog-cancel-text])))
(rf/reg-sub
  ::show-dialog?
  (fn [db]
    (get-in db [::db/show-dialog?] false)))