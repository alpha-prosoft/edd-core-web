(ns web.widgets.gallery.pages.snackbar-alert
  (:require
   [re-frame.core :as rf]
   [web.primitives.components :refer [RawButton RawGrid]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.snackbar-alert.events :as events]
   [web.widgets.snackbar-alert.views :refer [revoke-alert]]))

(defdemo alert-button [severity event]
  [RawGrid {}
   [RawButton {:el-id    (page/el :snackbar-alert severity)
               :on-click #(rf/dispatch [event {:message (str (name severity) " message")}])}
    (str (name severity) " alert")]])

(def ^:private props-doc
  [{:name :message            :type "string"  :default "—"      :doc "Alert text."}
   {:name :auto-hide-duration :type "ms"      :default "3000"   :doc "Closes the alert after this long."}
   {:name :anchor-origin      :type "map"     :default "bottom center" :doc "Snackbar position."}
   {:name :on-close-hook      :type "fn"      :default "—"      :doc "Called when the alert closes."}])

(defn register! []
  (page/register!
   {:id    :snackbar-alert
    :group "Widgets"
    :title "Snackbar alert"
    :doc   "Render revoke-alert once, then dispatch ::show-error-alert, ::show-warning-alert, ::show-info-alert or ::show-success-alert."
    :props props-doc
    :variants
    [(example {:id :severities :title "Severities"}
              [RawGrid {:container true :spacing 2}
               [alert-button :error ::events/show-error-alert]
               [alert-button :warning ::events/show-warning-alert]
               [alert-button :info ::events/show-info-alert]
               [alert-button :success ::events/show-success-alert]
               [revoke-alert]])]}))
