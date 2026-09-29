(ns web.primitives.gallery.pages.radio
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawRadioGroup]]))

(defdemo group [variant props]
  (let [state (r/atom {:value "a"})]
    (fn []
      [:div
       [RawRadioGroup
        (merge {:el-id     (page/el :radio variant)
                :value     (:value @state)
                :on-change (fn [^js e] (swap! state assoc :value (.. e -target -value)))
                :children  [{:value "a" :label "A"}
                            {:value "b" :label "B"}
                            {:value "c" :label "C"}]}
               props)]
       [:div {:data-testid (page/testid :radio variant :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :children  :type "vector of {:value :label}" :default "—" :doc "Options; each becomes a FormControlLabel with a Radio."}
   {:name :value     :type "string"  :default "—"     :doc "Selected option value."}
   {:name :on-change :type "fn"      :default "—"     :doc "Called with the change event."}
   {:name :label     :type "string"  :default "nil"   :doc "Group label."}
   {:name :row       :type "boolean" :default "false" :doc "Lay options out horizontally."}
   {:name :el-id     :type "keyword | string" :default "—" :doc "Lands as data-testid on the group."}])

(defn register! []
  (page/register!
   {:id    :radio
    :group "Inputs"
    :title "Radio group"
    :doc   "Select exactly one option from a small set."
    :props props-doc
    :variants
    [(example {:id :vertical :title "Vertical"}
              [group :vertical {}])

     (example {:id :row :title "Row"}
              [group :row {:row true}])]}))
