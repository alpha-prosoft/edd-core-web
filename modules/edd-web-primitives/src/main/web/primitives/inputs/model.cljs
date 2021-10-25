(ns web.primitives.inputs.model
  (:require [re-frame.core :as rf]
            [web.primitives.db :as db]))

(rf/reg-event-db
 ::set-date-picker-focused
 (fn [db [_ date-picker-state-id focused?]]
   (assoc-in db [::db/date-picker (keyword date-picker-state-id) :focused?] focused?)))

(rf/reg-event-db
 ::set-date-picker-touched
 (fn [db [_ date-picker-state-id]]
   (assoc-in db [::db/date-picker (keyword date-picker-state-id) :touched?] true)))

(rf/reg-sub
 ::date-picker-focused?
 (fn [db [_ date-picker-state-id]]
   (get-in db [::db/date-picker (keyword date-picker-state-id) :focused?] false)))

(rf/reg-sub
 ::date-picker-touched?
 (fn [db [_ date-picker-state-id]]
   (get-in db [::db/date-picker (keyword date-picker-state-id) :touched?] false)))

(rf/reg-event-db
 ::set-date-input-invalid
 (fn [db [_ date-picker-state-id invalid?]]
   (assoc-in db [::db/date-picker (keyword date-picker-state-id) :date-input-invalid?] invalid?)))

(rf/reg-sub
 ::date-input-invalid?
 (fn [db [_ date-picker-state-id]]
   (get-in db [::db/date-picker (keyword date-picker-state-id) :date-input-invalid?] false)))

(rf/reg-event-db
 ::set-icon-button-hovered
 (fn [db [_ icon-button-state-id state]]
   (assoc-in db [::db/icon-button icon-button-state-id :hovered?] state)))

(rf/reg-sub
 ::icon-button-hovered?
 (fn [db [_ icon-button-state-id]]
   (get-in db [::db/icon-button icon-button-state-id :hovered?])))
