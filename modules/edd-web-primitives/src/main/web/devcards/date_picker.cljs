(ns web.devcards.date-picker
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]
   [cljs-time.core :as time]
   [cljs-time.format :as format]

   [web.primitives.inputs.core :refer [EddDatePicker]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg date-picker
  "## Date picker"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker {:id "date-picker"
                     :value (:value @data-atom)
                     :invalid-date-message "Date is invalid"
                     :on-change (fn [x] (swap! data-atom merge {:value x}))}]))
  (r/atom {:value "2020-09-15"}))

(defcard-rg date-picker-invalid
  "## Invalid Date picker"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker {:id "date-picker-invalid"
                     :value (:value @data-atom)
                     :required true
                     :label "Please set required date"
                     :on-change (fn [x] (swap! data-atom merge {:value x}))}]))
  (r/atom {:invalid? true :value nil}))

(defcard-rg date-picker-today-disabled
  "## Disabled Date picker today"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker {:id "date-picker-today-disabled"
                     :value (:value @data-atom)
                     :required true
                     :disabled true
                     :on-change (fn [x] (swap! data-atom merge {:value x}))}]))
  (r/atom {:value (format/unparse (format/formatter "yyyy-MM-dd") (time/to-default-time-zone (js/Date.)))}))