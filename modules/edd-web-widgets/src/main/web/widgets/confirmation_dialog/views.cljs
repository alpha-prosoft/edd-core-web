(ns web.widgets.confirmation-dialog.views
  (:require
    [re-frame.core :as rf]
    [web.widgets.confirmation-dialog.subs :as subs]
    [web.widgets.confirmation-dialog.events :as events]
    ["@mui/material/Button" :default Button]
    ["@mui/material/Grid" :default Grid]
    ["@mui/material/Zoom" :default Zoom]

    ["@mui/material/index" :refer [Dialog DialogTitle DialogActions]]))

(defn ConfirmationDialog [{:keys [button-props proceed-button-props cancel-button-props]}]
  (let [show? @(rf/subscribe [::subs/show-dialog?])
        message @(rf/subscribe [::subs/dialog-message])
        proceed-text @(rf/subscribe [::subs/dialog-proceed-text])
        cancel-text @(rf/subscribe [::subs/dialog-cancel-text])]
    [:> Dialog {:disableEnforceFocus true
                :open                show?}
     [:> DialogTitle message]

     [:> DialogActions
      [:> Grid {:container true :spacing 2}
       [:> Grid {:item true :xs true}
        [:> Button
         (merge
           button-props
           proceed-button-props
           {:on-click #(rf/dispatch [::events/on-proceed])})
         proceed-text]]
       [:> Grid {:item true :xs true}
        [:> Button
         (merge
           button-props
           cancel-button-props
           {:on-click #(rf/dispatch [::events/on-cancel])})
         cancel-text]]]]]))

