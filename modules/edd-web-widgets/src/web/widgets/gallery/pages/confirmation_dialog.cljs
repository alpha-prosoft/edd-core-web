(ns web.widgets.gallery.pages.confirmation-dialog
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [web.primitives.components :refer [RawButton]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.confirmation-dialog.events :as events]
   [web.widgets.confirmation-dialog.views :refer [ConfirmationDialog]]))

(defdemo confirm-delete []
  (let [answer (r/atom "none")]
    (fn []
      [:div
       [RawButton {:el-id    (page/el :confirmation-dialog :basic)
                   :variant  "outlined"
                   :on-click #(rf/dispatch [::events/open-confirmation-dialog
                                            {:message         "Are you sure?"
                                             :proceed-text    "Yes, proceed"
                                             :cancel-text     "Nope, cancel"
                                             :on-proceed-func (fn [] (reset! answer "proceed"))
                                             :on-cancel-func  (fn [] (reset! answer "cancel"))}])}
        "Open dialog"]
       [:div {:data-testid (page/testid :confirmation-dialog :basic :readout)}
        (str "answer:" @answer)]
       [ConfirmationDialog {:button-props {:variant    "outlined"
                                           :full-width true
                                           :color      "secondary"}}]])))

(def ^:private props-doc
  [{:name :button-props         :type "map" :default "—" :doc "Props for both buttons."}
   {:name :proceed-button-props :type "map" :default "—" :doc "Extra props for the proceed button."}
   {:name :cancel-button-props  :type "map" :default "—" :doc "Extra props for the cancel button."}])

(defn register! []
  (page/register!
   {:id    :confirmation-dialog
    :group "Widgets"
    :title "Confirmation dialog"
    :doc   "Render ConfirmationDialog once, then open it with ::open-confirmation-dialog carrying the message, button texts and callbacks."
    :props props-doc
    :variants
    [(example {:id :basic :title "Proceed or cancel"}
              [confirm-delete])]}))
