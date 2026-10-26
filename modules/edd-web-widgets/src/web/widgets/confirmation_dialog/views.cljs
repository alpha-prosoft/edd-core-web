(ns web.widgets.confirmation-dialog.views
  (:require
   [re-frame.core :as rf]
   [web.widgets.confirmation-dialog.subs :as subs]
   [web.widgets.confirmation-dialog.events :as events]
   [web.primitives.components :refer [RawButton RawDialog RawGrid]]))

(defn ConfirmationDialog [{:keys [el-id button-props proceed-button-props cancel-button-props]
                           :or   {el-id :confirmation-dialog}}]
  (let [show? @(rf/subscribe [::subs/show-dialog?])
        message @(rf/subscribe [::subs/dialog-message])
        proceed-text @(rf/subscribe [::subs/dialog-proceed-text])
        cancel-text @(rf/subscribe [::subs/dialog-cancel-text])]
    [RawDialog {:el-id               el-id
                :disableEnforceFocus true
                :open                show?
                :title               message
                :actions
                [RawGrid {:container true :spacing 2 :sx {:width "100%"}}
                 [RawGrid {:size "grow"}
                  [RawButton
                   (merge
                    button-props
                    proceed-button-props
                    {:el-id    [el-id :proceed]
                     :on-click #(rf/dispatch [::events/on-proceed])})
                   proceed-text]]
                 [RawGrid {:size "grow"}
                  [RawButton
                   (merge
                    button-props
                    cancel-button-props
                    {:el-id    [el-id :cancel]
                     :on-click #(rf/dispatch [::events/on-cancel])})
                   cancel-text]]]}
     nil]))
