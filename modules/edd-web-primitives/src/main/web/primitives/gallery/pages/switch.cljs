(ns web.primitives.gallery.pages.switch
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawSwitch]]))

(defdemo toggling [variant props]
  (let [state (r/atom {:value (boolean (:value props))})]
    (fn []
      [:div
       [RawSwitch
        (merge {:el-id     (page/el :switch variant)
                :value     (:value @state)
                :on-change #(swap! state update :value not)}
               (dissoc props :value))]
       [:div {:data-testid (page/testid :switch variant :readout)}
        (str "on:" (:value @state))]])))

(defdemo two-value []
  (let [state (r/atom {:selected :one})]
    (fn []
      [:div
       [RawSwitch {:el-id       (page/el :switch :two-value)
                   :left-label  "One"
                   :right-label "Two"
                   :left-value  :one
                   :right-value :two
                   :value       (:selected @state)
                   :on-change   #(swap! state assoc :selected %)}]
       [:div {:data-testid (page/testid :switch :two-value :readout)}
        (str "selected:" (name (:selected @state)))]])))

(def ^:private props-doc
  [{:name :value           :type "boolean | any" :default "—" :doc "Current value. Boolean for a plain switch, one of left/right value in two-value mode."}
   {:name :on-change       :type "fn"      :default "—"     :doc "Called with the new value."}
   {:name :label           :type "string"  :default "\"\""  :doc "Label for a plain switch."}
   {:name :label-placement :type "string"  :default "\"end\"" :doc "Label position."}
   {:name :left-label      :type "string"  :default "—"     :doc "Two-value mode: label of the left option."}
   {:name :right-label     :type "string"  :default "—"     :doc "Two-value mode: label of the right option."}
   {:name :left-value      :type "any"     :default "—"     :doc "Two-value mode: value when switch is off."}
   {:name :right-value     :type "any"     :default "—"     :doc "Two-value mode: value when switch is on."}
   {:name :disabled        :type "boolean" :default "false" :doc "Disables interaction."}
   {:name :el-id           :type "keyword | string" :default "—" :doc "Lands as id on the native input."}])

(defn register! []
  (page/register!
   {:id    :switch
    :group "Inputs"
    :title "Switch"
    :doc   "Switches toggle a single setting. Provide left/right labels and values to use it as a two-option selector."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [toggling :basic {:value true}])

     (example {:id :labelled :title "With label"}
              [toggling :labelled {:value false :label "Enable"}])

     (example {:id :label-start :title "Label start"}
              [toggling :label-start {:value false :label "Enable" :label-placement "start"}])

     (example {:id :disabled :title "Disabled"}
              [RawSwitch {:el-id    (page/el :switch :disabled)
                          :value    true
                          :disabled true
                          :label    "Disabled"}])

     (example {:id :two-value :title "Two values"}
              [two-value])]}))
