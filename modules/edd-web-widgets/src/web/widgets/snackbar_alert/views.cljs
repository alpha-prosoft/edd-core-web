(ns web.widgets.snackbar-alert.views
  (:require
   [re-frame.core :as rf]
   [web.primitives.components :refer [RawSnackbar]]
   [web.widgets.snackbar-alert.subs :as subs]
   [web.widgets.snackbar-alert.events :as events]))

(defn revoke-alert []
  (let [message @(rf/subscribe [::subs/message])
        severity @(rf/subscribe [::subs/severity])
        auto-hide-duration @(rf/subscribe [::subs/auto-hide-duration])
        anchor-origin @(rf/subscribe [::subs/anchor-origin])]
    (when (and (some? message) (some? severity) (some? anchor-origin))
      [RawSnackbar {:el-id              :snackbar-alert
                    :anchor-origin      anchor-origin
                    :open               (some? message)
                    :on-close           #(rf/dispatch [::events/close])
                    :key                "snackbar-alert"
                    :auto-hide-duration auto-hide-duration
                    :message            message
                    :alert-props        {:severity severity}}
       nil])))
