(ns web.widgets.snackbar-alert.events
  (:require
    [re-frame.core :as rf]
    [web.widgets.snackbar-alert.db :as db]))

(rf/reg-event-db
  :init-snackbar-alert-db
  (fn [db]
    (merge db db/default-db)))

(rf/reg-event-fx
  ::close
  (fn [{:keys [db]}]
    (let [on-close-hook (get-in db [::db/on-close-hook])]
      (when (some? on-close-hook)
        (on-close-hook))
      {:db (-> db
               (assoc-in [::db/message] nil)
               (assoc-in [::db/severity] nil))})))

(rf/reg-event-db
  ::show-alert
  (fn [db [_ {:keys [message auto-hide-duration severity anchor-origin on-close-hook]
              :or   {auto-hide-duration 3000
                     anchor-origin      {:vertical "bottom" :horizontal "center"}}}]]
    (-> db
        (assoc-in [::db/on-close-hook] on-close-hook)
        (assoc-in [::db/anchor-origin] anchor-origin)
        (assoc-in [::db/auto-hide-duration] auto-hide-duration)
        (assoc-in [::db/message] message)
        (assoc-in [::db/severity] severity))))

(rf/reg-event-fx
  ::show-error-alert
  (fn [_ [_ props]]
    {:dispatch [::show-alert (merge props {:severity "error"})]}))

(rf/reg-event-fx
  ::show-warning-alert
  (fn [_ [_ props]]
    {:dispatch [::show-alert (merge props {:severity "warning"})]}))

(rf/reg-event-fx
  ::show-info-alert
  (fn [_ [_ props]]
    {:dispatch [::show-alert (merge props {:severity "info"})]}))

(rf/reg-event-fx
  ::show-success-alert
  (fn [_ [_ props]]
    {:dispatch [::show-alert (merge props {:severity "success"})]}))