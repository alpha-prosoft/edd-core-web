(ns web.primitives.gallery.pages.time-picker
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawTimePicker]]))

(defn- time-label [^js d]
  (when d
    (str (.padStart (str (.getHours d)) 2 "0") ":" (.padStart (str (.getMinutes d)) 2 "0"))))

(defdemo picker [variant props]
  (let [state (r/atom {:value nil})]
    (fn []
      [:div
       [RawTimePicker
        (merge {:el-id     (page/el :time-picker variant)
                :label     "Time"
                :value     (:value @state)
                :on-change #(swap! state assoc :value %)}
               props)]
       [:div {:data-testid (page/testid :time-picker variant :readout)}
        (str "value:" (time-label (:value @state)))]])))

(def ^:private props-doc
  [{:name :value     :type "Date"    :default "nil"       :doc "Selected time (controlled)."}
   {:name :on-change :type "fn"      :default "—"         :doc "Called with a Date, or nil."}
   {:name :label     :type "string"  :default "—"         :doc "Field label."}
   {:name :ampm      :type "boolean" :default "false"     :doc "12h clock."}
   {:name :format    :type "string"  :default "\"HH:mm\"" :doc "Display format."}
   {:name :disabled  :type "boolean" :default "false"     :doc "Disables the field."}
   {:name :slotProps :type "map"     :default "—"         :doc "MUI slot props; :textField is merged over the defaults."}
   {:name :el-id     :type "keyword | string" :default "—" :doc "Lands as id on the text field input."}])

(defn register! []
  (page/register!
   {:id    :time-picker
    :group "Inputs"
    :title "Time picker"
    :doc   "24h hours/minutes picker in a mobile dialog. Values are Date objects."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [picker :basic {}])

     (example {:id :disabled :title "Disabled"}
              [picker :disabled {:disabled true}])]}))
