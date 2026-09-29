(ns web.primitives.gallery.pages.checkbox
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawCheckbox]]))

(defdemo toggling [variant props]
  (let [state (r/atom {:checked (boolean (:checked props))})]
    (fn []
      [:div
       [RawCheckbox
        (merge {:el-id     (page/el :checkbox variant)
                :checked   (:checked @state)
                :on-change #(swap! state update :checked not)}
               (dissoc props :checked))]
       [:div {:data-testid (page/testid :checkbox variant :readout)}
        (str "checked:" (:checked @state))]])))

(def ^:private props-doc
  [{:name :checked         :type "boolean" :default "false" :doc "Controlled checked state."}
   {:name :indeterminate   :type "boolean" :default "false" :doc "Shows the mixed (dash) state."}
   {:name :label           :type "string"  :default "\"\""  :doc "Text rendered next to the box."}
   {:name :label-placement :type "\"end\" | \"start\" | \"top\" | \"bottom\"" :default "\"end\"" :doc "Where the label sits."}
   {:name :disabled        :type "boolean" :default "false" :doc "Disables interaction."}
   {:name :on-change       :type "fn"      :default "—"     :doc "Called when the box is toggled."}
   {:name :el-id           :type "keyword | string" :default "—" :doc "Lands as id on the native input."}])

(defn register! []
  (page/register!
   {:id    :checkbox
    :group "Inputs"
    :title "Checkbox"
    :doc   "Checkboxes select one or more items from a set, or toggle a single option on/off."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [toggling :basic {}])

     (example {:id :labelled :title "With label"}
              [toggling :labelled {:label "Accept"}])

     (example {:id :label-start :title "Label placement start"}
              [toggling :label-start {:label "Accept" :label-placement "start"}])

     (example {:id :checked :title "Initially checked"}
              [toggling :checked {:checked true :label "On"}])

     (example {:id :disabled :title "Disabled"}
              [RawCheckbox {:el-id    (page/el :checkbox :disabled)
                            :checked  true
                            :disabled true
                            :label    "Disabled"}])]}))
