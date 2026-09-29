(ns web.primitives.gallery.pages.slider
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawSlider]]))

(defdemo sliding [variant props]
  (let [state (r/atom {:value (or (:value props) 30)})]
    (fn []
      [:div {:style {:width "300px"}}
       [RawSlider
        (merge {:el-id     (page/el :slider variant)
                :value     (:value @state)
                :on-change (fn [^js _e v] (swap! state assoc :value v))}
               (dissoc props :value))]
       [:div {:data-testid (page/testid :slider variant :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :value     :type "number"  :default "—"     :doc "Current value."}
   {:name :on-change :type "fn"      :default "—"     :doc "(event, value) handler."}
   {:name :min       :type "number"  :default "0"     :doc "Minimum."}
   {:name :max       :type "number"  :default "100"   :doc "Maximum."}
   {:name :step      :type "number"  :default "1"     :doc "Step size."}
   {:name :marks     :type "boolean | vector" :default "false" :doc "Show step marks."}
   {:name :disabled  :type "boolean" :default "false" :doc "Disables interaction."}
   {:name :el-id     :type "keyword | string" :default "—" :doc "Stable id for tests."}])

(defn register! []
  (page/register!
   {:id    :slider
    :group "Inputs"
    :title "Slider"
    :doc   "Select a value from a range. All MUI Slider props pass through."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [sliding :basic {}])

     (example {:id :steps :title "With marks/steps"}
              [sliding :steps {:value 50 :step 10 :marks true}])

     (example {:id :disabled :title "Disabled"}
              [:div {:style {:width "300px"}}
               [RawSlider {:el-id (page/el :slider :disabled) :value 40 :disabled true}]])]}))
