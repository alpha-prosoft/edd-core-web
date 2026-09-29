(ns web.primitives.gallery.pages.date-picker
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [EddDatePicker]]))

(defdemo picker [variant props]
  (let [state (r/atom {:value (:value props)})]
    (fn []
      [:div
       [EddDatePicker
        (merge {:el-id     (page/el :date-picker variant)
                :label     "Date"
                :value     (:value @state)
                :on-change #(swap! state assoc :value %)}
               (dissoc props :value))]
       [:div {:data-testid (page/testid :date-picker variant :readout)}
        (str "value:" (:value @state))]])))

(def ^:private props-doc
  [{:name :value                    :type "string \"yyyy-MM-dd\"" :default "nil" :doc "ISO date string (controlled)."}
   {:name :on-change                :type "fn"      :default "—"     :doc "Called with an ISO date string, or nil when cleared. Out-of-range dates are still reported so typing is never wiped."}
   {:name :label                    :type "string"  :default "—"     :doc "Field label."}
   {:name :min-date                 :type "string"  :default "\"1900-01-01\"" :doc "Earliest allowed date."}
   {:name :max-date                 :type "string"  :default "\"2099-12-31\"" :doc "Latest allowed date."}
   {:name :disable-past             :type "boolean" :default "false" :doc "Disallow dates before today."}
   {:name :required                 :type "boolean" :default "false" :doc "Show required error when empty after blur."}
   {:name :invalid-date-message     :type "string"  :default "\"Invalid date\"" :doc "Helper text when the typed date is out of range."}
   {:name :on-invalid-hook          :type "fn"      :default "—"     :doc "Called with true/false when validity changes."}
   {:name :read-only                :type "boolean" :default "false" :doc "Renders as a read only text field."}
   {:name :read-only-with-underline :type "boolean" :default "false" :doc "Read only, keeps underline."}
   {:name :el-id                    :type "keyword | string" :default "—" :doc "Lands on the field input; slot buttons derive their ids from it."}])

(defn register! []
  (page/register!
   {:id    :date-picker
    :group "Inputs"
    :title "Date picker"
    :doc   "Date input in dd.MM.yyyy format with a calendar popup. Values are ISO strings. Validation runs on blur, so typing day-first never clears the field."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [picker :basic {}])

     (example {:id :prefilled :title "Prefilled"}
              [picker :prefilled {:value "2020-09-15"}])

     (example {:id :min-max :title "Min 2021 / max 2025"}
              [picker :min-max {:min-date "2021-01-01" :max-date "2025-12-31"}])

     (example {:id :required :title "Required"}
              [picker :required {:required true}])

     (example {:id :read-only :title "Read only"}
              [EddDatePicker {:el-id     (page/el :date-picker :read-only)
                              :label     "Date"
                              :value     "2020-09-15"
                              :read-only true}])]}))
