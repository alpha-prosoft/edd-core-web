(ns web.primitives.gallery.pages.select
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.data-display.core :refer [RawMenuItem]]
   [web.primitives.inputs.core :refer [RawSelect RawFormSelect]]))

(defdemo simple []
  (let [state (r/atom {:value "One"})]
    (fn []
      [:div
       [RawSelect {:el-id     (page/el :select :simple)
                   :options   ["One" "Two" "Three"]
                   :value     (:value @state)
                   :on-change #(swap! state assoc :value %)}]
       [:div {:data-testid (page/testid :select :simple :readout)}
        (str "value:" (:value @state))]])))

(defdemo labelled []
  (let [state (r/atom {:value ""})]
    (fn []
      [:div
       [RawSelect {:el-id        (page/el :select :labelled)
                   :displayEmpty true
                   :options      [{:value "" :label [:i "Auto"]}
                                  {:value "1" :label "1st"}
                                  {:value "2" :label "2nd"}]
                   :value        (:value @state)
                   :on-change    #(swap! state assoc :value %)}]
       [:div {:data-testid (page/testid :select :labelled :readout)}
        (str "value:" (:value @state))]])))

(defdemo form []
  (let [state (r/atom {:value "A"})]
    (fn []
      [:div
       [RawFormSelect {:el-id       (page/el :select :form)
                       :input-label "Group"
                       :value       (:value @state)
                       :on-change   (fn [^js e] (swap! state assoc :value (.. e -target -value)))
                       :children    (mapv
                                     (fn [o]
                                       (r/as-element
                                        (RawMenuItem {:key o :value o} o)))
                                     ["A" "B" "C"])}]
       [:div {:data-testid (page/testid :select :form :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :options                  :type "vector"  :default "[]"    :doc "RawSelect: plain values, or {:value :label} maps when the label differs."}
   {:name :children                 :type "elements" :default "—"    :doc "RawFormSelect: MenuItem elements (use r/as-element)."}
   {:name :value                    :type "any"     :default "first option" :doc "Selected value."}
   {:name :on-change                :type "fn"      :default "—"     :doc "RawSelect: called with the value. RawFormSelect: called with the event."}
   {:name :input-label              :type "string"  :default "—"     :doc "RawFormSelect: label above the field."}
   {:name :required                 :type "boolean" :default "false" :doc "Marks the field required."}
   {:name :helper-text              :type "string"  :default "nil"   :doc "Text under the field."}
   {:name :read-only                :type "boolean" :default "false" :doc "Read only mode."}
   {:name :read-only-with-underline :type "boolean" :default "false" :doc "Read only, keeps underline."}
   {:name :el-id                    :type "keyword | string" :default "—" :doc "Stable id for tests."}])

(defn register! []
  (page/register!
   {:id    :select
    :group "Inputs"
    :title "Select"
    :doc   "Pick one value from a list. `RawSelect` takes plain options; `RawFormSelect` wraps a labelled form control and takes MenuItem children."
    :props props-doc
    :variants
    [(example {:id :simple :title "Simple select"}
              [simple])

     (example {:id :labelled :title "Labelled options"}
              [labelled])

     (example {:id :form :title "Form select"}
              [form])]}))
