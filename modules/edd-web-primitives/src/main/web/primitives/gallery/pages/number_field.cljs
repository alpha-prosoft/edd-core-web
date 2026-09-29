(ns web.primitives.gallery.pages.number-field
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawNumberField RawPercentField]]))

(defdemo number [variant props]
  (let [state (r/atom {:value nil})]
    (fn []
      [:div
       [RawNumberField
        (merge {:el-id         (page/el :number-field variant)
                :label         "Amount"
                :default-value 0
                :on-change     #(swap! state assoc :value %)}
               props)]
       [:div {:data-testid (page/testid :number-field variant :readout)}
        (str "cents:" (:value @state))]])))

(defdemo percent [variant props]
  (let [state (r/atom {:value nil})]
    (fn []
      [:div
       [RawPercentField
        (merge {:el-id         (page/el :number-field variant)
                :label         "Percent"
                :suffix        "%"
                :default-value 0
                :on-change     #(swap! state assoc :value %)}
               props)]
       [:div {:data-testid (page/testid :number-field variant :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :default-value   :type "number"  :default "—"      :doc "Initial value. Number field: cents. Percent field: fraction (0.25 = 25%)."}
   {:name :on-change       :type "fn"      :default "—"      :doc "Number field: called with cents. Percent field: called with fraction."}
   {:name :separator       :type "string"  :default "\".\""  :doc "Thousands separator for the number field."}
   {:name :formatting-func :type "fn"      :default "nil"    :doc "Custom display formatter."}
   {:name :amount-scaling  :type "string"  :default "\"full\"" :doc "Scaling used for display formatting."}
   {:name :prefix          :type "node"    :default "nil"    :doc "Start adornment."}
   {:name :suffix          :type "node"    :default "nil"    :doc "End adornment."}
   {:name :read-only       :type "boolean" :default "false"  :doc "Read only mode."}
   {:name :auto-focus      :type "boolean" :default "false"  :doc "Focus on mount."}
   {:name :el-id           :type "keyword | string" :default "—" :doc "Lands as id on the native input."}])

(defn register! []
  (page/register!
   {:id    :number-field
    :group "Inputs"
    :title "Number & percent field"
    :doc   "Numeric inputs with formatting. Typing `1k`, `2m` or `3b` expands to thousands, millions and billions. Values are reported in cents."
    :props props-doc
    :variants
    [(example {:id :number :title "Number"}
              [number :number {}])

     (example {:id :separator :title "Number with separator"}
              [number :separator {:separator " "}])

     (example {:id :read-only :title "Number read only"}
              [RawNumberField {:el-id         (page/el :number-field :read-only)
                               :label         "Read only"
                               :default-value 12345
                               :read-only     true}])

     (example {:id :percent :title "Percent"}
              [percent :percent {}])]}))
