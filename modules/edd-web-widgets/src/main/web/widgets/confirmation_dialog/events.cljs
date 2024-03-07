(ns web.widgets.confirmation-dialog.events
  (:require
    [re-frame.core :as rf]
    [web.widgets.confirmation-dialog.db :as db]))

(rf/reg-event-db
  ::open-confirmation-dialog
  (fn [db [_ {:keys [message proceed-text cancel-text
                     on-proceed-func on-cancel-func]
              :or {message ""
                   proceed-text "Proceed"
                   cancel-text "Cancel"
                   on-proceed-func nil
                   on-cancel-func nil}}]]
    (assoc db
      ::db/dialog-message message
      ::db/dialog-proceed-text proceed-text
      ::db/dialog-cancel-text cancel-text
      ::db/on-proceed-func on-proceed-func
      ::db/on-cancel-func on-cancel-func
      ::db/show-dialog? true)))

(rf/reg-event-fx
  ::on-proceed
  (fn [{:keys [db]}]
    (let [on-proceed-func (get-in db [::db/on-proceed-func])]
      (when (some? on-proceed-func)
        (on-proceed-func))
      {:db (assoc-in db [::db/show-dialog?] false)})))

(rf/reg-event-fx
  ::on-cancel
  (fn [{:keys [db]}]
    (let [on-cancel-func (get-in db [::db/on-cancel-func])]
      (when (some? on-cancel-func)
        (on-cancel-func))
      {:db (assoc-in db [::db/show-dialog?] false)})))
