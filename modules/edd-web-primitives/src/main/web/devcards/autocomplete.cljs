(ns web.devcards.autocomplete
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [reagent.core :as r]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawAutocomplete RawCheckbox RawMenuItem]]))

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

(defcard-rg :autocomplete
  "## Autocomplete"
  (apply-stiles
   [RawAutocomplete
    {:id               "autocomplete"
     :options          months-list
     :label            "Start typing"
     :on-change        (fn [_ option] (print option))
     :get-option-label (fn [option]
                         (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg :autocomplete-with-helper-text
  "## Autocomplete with helper text"
  (apply-stiles
   [RawAutocomplete
    {:id                  "autocomplete-with-helper-text"
     :options             months-list
     :label               "Start typing"
     :FormHelperTextProps {:style {:text-align "center"}}
     :helper-text         "Helper text"
     :on-change           (fn [_ option] (print option))
     :get-option-label    (fn [option]
                            (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg :autocomplete-disabled
  "## Autocomplete disabled"
  (apply-stiles
   [RawAutocomplete
    {:id               "autocomplete-disabled"
     :options          months-list
     :label            "Disabled"
     :disabled         true
     :on-change        (fn [_ option] (print option))
     :get-option-label (fn [option]
                         (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))

(defcard-rg :autocomplete-prefilled
  "## Autocomplete prefilled"
  (fn [data-atom _]
    (apply-stiles
     [RawAutocomplete
      {:id                   "autocomplete-prefilled"
       :options              months-list
       :label                "Autocomplete prefilled"
       :value                (:selected @data-atom)
       :isOptionEqualToValue (fn [_option _value]
                               (let [item (js->clj _option :keywordize-keys true)
                                     value (js->clj _value :keywordize-keys true)]
                                 (= item value)))
       :on-change            (fn [_ option] (swap! data-atom merge
                                                   {:selected (js->clj option :keywordize-keys true)}))
       :get-option-label     (fn [option]
                               (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))
  (r/atom {:selected {:id 4 :name "May"}}))

(defcard-rg :autocomplete-with-loading
  "## Autocomplete with loading"
  (fn [data-atom _]
    (apply-stiles
     [RawAutocomplete
      {:id               "autocomplete-with-loading"
       :label            "Start typing"
       :loading          @data-atom
       :on-change        (fn [_ option] (print option))
       :on-input-change  (fn [] (swap! data-atom not))
       :get-option-label (fn [option]
                           (get-in (js->clj option :keywordize-keys true) [:name] ""))}]))
  (r/atom false))

(defcard-rg :autocomplete-with-multiple-options
  "## Autocomplete with multiple options"
  (fn [data-atom _]
    (apply-stiles
     [RawAutocomplete
      {:id                   "autocomplete-with-multiple-options"
       :multiple             true
       :disableCloseOnSelect true
       :options              (:list @data-atom)
       :label                "Choose several items"
       :value                (:selected @data-atom)
       :onChange             (fn [_ option]
                               (print option)
                               (swap! data-atom merge
                                      {:selected (js->clj option :keywordize-keys true)}))
       :on-blur              #(let [new-list (vec (distinct (concat
                                                             (:selected @data-atom)
                                                             (sort-by :id (:list @data-atom)))))]
                                (swap! data-atom merge {:list new-list}))
       :getOptionLabel       (fn [option]
                               (get-in (js->clj option :keywordize-keys true) [:name] ""))
       :isOptionEqualToValue (fn [_option _value]
                               (let [item (js->clj _option :keywordize-keys true)
                                     value (js->clj _value :keywordize-keys true)]
                                 (= item value)))
       :renderOption         (fn [state option]
                               (let [item (js->clj option :keywordize-keys true)
                                     clj-state (js->clj state :keywordize-keys true)]
                                 (r/as-element [RawMenuItem (merge
                                                             {:key (:id item)}
                                                             (dissoc clj-state :aria-selected))
                                                [RawCheckbox {:checked (:aria-selected clj-state)
                                                              :label   (str (get-in item [:name] ""))}]])))}]))

  (r/atom {:selected
           [{:id 3 :name "April"}
            {:id 5 :name "June"}]
           :list (vec (distinct (concat
                                 [{:id 3 :name "April"}
                                  {:id 5 :name "June"}]
                                 months-list)))}))
