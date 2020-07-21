(ns web.devcards.autocomplete
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [reagent.core :as r]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.lab.core :refer [RawAutocomplete]]
   [web.primitives.inputs.core :refer [RawCheckbox]]))

(def months-list [{:id 0 :name "January"}
                  {:id 1 :name "February"}
                  {:id 2 :name "March"}
                  {:id 3 :name "April"}
                  {:id 4 :name "May"}
                  {:id 5 :name "June"}
                  {:id 6 :name "July"}
                  {:id 7 :name "August"}
                  {:id 8 :name "September"}
                  {:id 9 :name "October"}
                  {:id 10 :name "November"}
                  {:id 11 :name "December"}])

(defcard-rg autocomplete
  "## Autocomplete"
  (apply-stiles [RawAutocomplete
                 {:id "autocomplete"
                  :options        months-list
                  :label "Start typing"
                  :get-option-label (fn [option]
                                      (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg autocomplete-with-helper-text
  "## Autocomplete with helper text"
  (apply-stiles [RawAutocomplete
                 {:id                  "autocomplete-with-helper-text"
                  :options             months-list
                  :label               "Start typing"
                  :FormHelperTextProps {:style {:text-align "center"}}
                  :helper-text         "Helper text"
                  :get-option-label    (fn [option]
                                         (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg autocomplete-disabled
  "## Autocomplete disabled"
  (apply-stiles [RawAutocomplete
                 {:id "autocomplete-disabled"
                  :options        months-list
                  :label "Disabled"
                  :disabled       true
                  :get-option-label (fn [option]
                                      (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg autocomplete-prefilled
  "## Autocomplete prefilled"
  (apply-stiles [RawAutocomplete
                 {:id "autocomplete-prefilled"
                  :options        months-list
                  :label "Autocomplete prefilled"
                  :value          (clj->js (first (filter #(= (:id %) 6) months-list)))
                  :get-option-label (fn [option]
                                      (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg autocomplete-with-loading
  "## Autocomplete with loading"
  (fn [data-atom _]
    (apply-stiles [RawAutocomplete
                   {:id "autocomplete-with-loading"
                    :label          "Start typing"
                    :loading        @data-atom
                    :on-input-change (fn [] (swap! data-atom not))
                    :get-option-label (fn [option]
                                        (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))
  (r/atom false))

(defcard-rg autocomplete-with-multiple-options
  "## Autocomplete with multiple options"
  (fn [data-atom _]
    (apply-stiles [RawAutocomplete
                   {:id "autocomplete-with-multiple-options"
                    :multiple true
                    :disableCloseOnSelect true
                    :options        (:list @data-atom)
                    :label          "Choose several items"
                    :value (:selected @data-atom)
                    :on-change (fn [_ value]
                                 (let [items (js->clj value :keywordize-keys true)]
                                   (swap! data-atom merge {:selected items})))
                    :on-blur (fn []
                               (let [new-list (vec (distinct (concat
                                                              (:selected @data-atom)
                                                              (sort-by :id (:list @data-atom)))))]
                                 (swap! data-atom merge {:list new-list})))

                    :get-option-label (fn [option]
                                        (get-in (js->clj option :keywordize-keys true) [:name] ""))
                    :get-option-selected (fn [_option _value]
                                           (let [item (js->clj _option :keywordize-keys true)
                                                 value (js->clj _value :keywordize-keys true)]
                                             (= item value)))
                    :render-option
                    (fn [option state]
                      (let [item (js->clj option :keywordize-keys true)
                            selected (:selected (js->clj state :keywordize-keys true))]
                        (r/as-element
                         [RawCheckbox {:checked selected
                                       :label (get-in item [:name] "")}])))}]))

  (r/atom {:selected
           [{:id 3 :name "April"}
            {:id 5 :name "June"}]
           :list (vec (distinct (concat
                                 [{:id 3 :name "April"}
                                  {:id 5 :name "June"}]
                                 months-list)))}))
