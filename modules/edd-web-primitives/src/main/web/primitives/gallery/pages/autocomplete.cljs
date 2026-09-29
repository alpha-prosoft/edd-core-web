(ns web.primitives.gallery.pages.autocomplete
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.lab.core :refer [RawAutocomplete]]))

(defdemo single-select []
  (let [state (r/atom {:value nil})]
    (fn []
      [:div {:style {:width "320px"}}
       [RawAutocomplete {:el-id     (page/el :autocomplete :basic)
                         :label     "Pick one"
                         :options   ["Alpha" "Beta" "Gamma"]
                         :value     (:value @state)
                         :on-change (fn [^js _e v] (swap! state assoc :value v))}]
       [:div {:data-testid (page/testid :autocomplete :basic :readout)}
        (str "value:" (:value @state))]])))

(defdemo multi-select []
  (let [state (r/atom {:value []})]
    (fn []
      [:div {:style {:width "320px"}}
       [RawAutocomplete {:el-id     (page/el :autocomplete :multiple)
                         :label     "Pick many"
                         :multiple  true
                         :options   ["Red" "Green" "Blue"]
                         :value     (:value @state)
                         :on-change (fn [^js _e v] (swap! state assoc :value (js->clj v)))}]
       [:div {:data-testid (page/testid :autocomplete :multiple :readout)}
        (str "count:" (count (:value @state)))]])))

(def ^:private props-doc
  [{:name :options              :type "vector"  :default "[]"    :doc "Items to choose from."}
   {:name :value                :type "any"     :default "nil"   :doc "Currently selected value (or vector when :multiple)."}
   {:name :on-change            :type "fn"      :default "—"     :doc "(event, value) -> handler called on selection."}
   {:name :label                :type "string"  :default "\"\""  :doc "Field label."}
   {:name :multiple             :type "boolean" :default "false" :doc "Allow selecting several values (renders chips)."}
   {:name :required             :type "boolean" :default "false" :doc "Marks the field as required."}
   {:name :error                :type "boolean" :default "false" :doc "Renders the field in an error state."}
   {:name :helper-text          :type "string"  :default "nil"   :doc "Text shown under the field."}
   {:name :getOptionSelected    :type "fn"      :default "—"     :doc "Custom equality between option and value."}
   {:name :render-tags          :type "fn"      :default "—"     :doc "Custom rendering of selected chips (multiple mode)."}
   {:name :FormHelperTextProps  :type "map"     :default "nil"   :doc "Props forwarded to the helper text slot."}
   {:name :variant              :type "string"  :default "\"standard\"" :doc "Underlying text field variant."}
   {:name :el-id                :type "keyword | string" :default "—" :doc "Stable id, lands on the native input for tests."}])

(defn register! []
  (page/register!
   {:id    :autocomplete
    :group "Inputs"
    :title "Autocomplete"
    :doc   "A text input with a suggestion popup. It has many props — the common ones drive selection, the rest customise rendering and validation. Expand the Props panel below for the full list."
    :props props-doc
    :variants
    [(example {:id :basic :title "Single select"}
              [single-select])

     (example {:id :multiple :title "Multiple select (chips)"}
              [multi-select])]}))
