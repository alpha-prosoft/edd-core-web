(ns web.widgets.snackbar-alert.views
  (:require
    [re-frame.core :as rf]
    ["@mui/material/Snackbar" :default Snackbar]
    ["@mui/material/Grid" :default Grid]
    ["@mui/material/Alert" :default Alert]
    [web.widgets.snackbar-alert.subs :as subs]
    [web.widgets.snackbar-alert.events :as events]))

(defn revoke-alert []
  (let [message @(rf/subscribe [::subs/message])
        severity @(rf/subscribe [::subs/severity])
        auto-hide-duration @(rf/subscribe [::subs/auto-hide-duration])
        anchor-origin @(rf/subscribe [::subs/anchor-origin])]
    (when (and (some? message) (some? severity) (some? anchor-origin))
      [:> Snackbar {:anchor-origin     anchor-origin
                    :open               (some? message)
                    :on-close           #(rf/dispatch [::events/close])
                    :key                "snackbar-alert"
                    :auto-hide-duration auto-hide-duration}
        [:> Alert {:severity severity}
         message]])))