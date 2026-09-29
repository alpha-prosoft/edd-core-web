(ns web.primitives.gallery.pages.button
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawButton]]
   [web.primitives.testing.el-id :refer [el-id]]
   [web.primitives.gallery.pages.button-elements :as e]))

(defdemo counting-button [variant label props]
  (let [state (r/atom 0)]
    (fn []
      [:div
       [RawButton
        (merge {:el-id (e/variant variant) :on-click #(swap! state inc)} props)
        label]
       [:div {:data-testid (el-id (e/variant-readout variant))}
        (str "clicks:" @state)]])))

(def ^:private props-doc
  [{:name :variant  :type "\"text\" | \"contained\" | \"outlined\"" :default "\"text\"" :doc "Visual emphasis of the button."}
   {:name :color    :type "\"primary\" | \"secondary\"" :default "\"primary\"" :doc "Theme color."}
   {:name :size     :type "\"small\" | \"medium\" | \"large\"" :default "\"medium\"" :doc "Button size."}
   {:name :disabled :type "boolean" :default "false" :doc "Disables interaction."}
   {:name :on-click :type "fn" :default "—" :doc "Click handler."}
   {:name :el-id    :type "keyword | string" :default "—" :doc "Stable test id; lands as data-testid on the button."}])

(defn register! []
  (page/register!
   {:id    :button
    :group "Inputs"
    :title "Button"
    :doc   "Buttons trigger an action. Use `variant` for emphasis and `color` for intent. Every button accepts an `:el-id` so tests can select it reliably."
    :props props-doc
    :variants
    [(example {:id :text :title "Text (default)"}
              [counting-button :text "Text" {}])

     (example {:id :contained :title "Contained"}
              [counting-button :contained "Contained" {:variant "contained"}])

     (example {:id :outlined :title "Outlined"}
              [counting-button :outlined "Outlined" {:variant "outlined"}])

     (example {:id :secondary :title "Contained secondary"}
              [counting-button :secondary "Secondary" {:variant "contained" :color "secondary"}])

     (example {:id :large :title "Large"}
              [counting-button :large "Large" {:variant "outlined" :size "large"}])

     (example {:id :disabled :title "Disabled"}
              [RawButton {:el-id e/disabled :variant "contained" :disabled true}
               "Disabled"])]}))
