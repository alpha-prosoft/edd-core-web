(ns web.devcards.snackbar-alert
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   ["@mui/material/Grid" :default Grid]
   ["@mui/material/Button" :default Button]
   [web.widgets.snackbar-alert.views :as views]
   [web.widgets.snackbar-alert.events :as events]
   [re-frame.core :as rf]))

(defcard-rg :snackbar-alert
  "## Snackbar Alert"
  (fn []
    [:> Grid {:container true :spacing 2}
     [:> Grid {:item true}
      [:> Button {:on-click #(rf/dispatch [::events/show-error-alert
                                           {:message       "Error message"
                                            :on-close-hook (fn [] (print "Close error alert"))}])}
       "Error alert"]]
     [:> Grid {:item true}
      [:> Button {:on-click #(rf/dispatch [::events/show-warning-alert
                                           {:message       "Warning message"
                                            :on-close-hook (fn [] (print "Close warning alert"))}])}
       "Warning alert"]]
     [:> Grid {:item true}
      [:> Button {:on-click #(rf/dispatch [::events/show-info-alert
                                           {:message       "Info message"
                                            :on-close-hook (fn [] (print "Close info alert"))}])}
       "Info alert"]]
     [:> Grid {:item true}
      [:> Button {:on-click #(rf/dispatch [::events/show-success-alert
                                           {:message       "Success message"
                                            :on-close-hook (fn [] (print "Close success alert"))}])}
       "Success alert"]]
     (views/revoke-alert)]))

