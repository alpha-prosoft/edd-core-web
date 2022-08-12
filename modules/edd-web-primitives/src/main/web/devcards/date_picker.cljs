(ns web.devcards.date-picker
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]
   [cljs-time.core :as time]
   [cljs-time.format :as format]

   [web.primitives.components :refer [EddDatePicker]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg :date-picker
  "## Date picker"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker {:id                   (str ::date-picker)
                     :value                (:value @data-atom)
                     :invalid-date-message "Date is invalid"
                     :on-invalid-hook #(print "invalid date: " %)
                     :on-change            (fn [x] (doall (print "-date- :" x)
                                                          (swap! data-atom merge {:value x})))}]))
  (r/atom {:value "2020-09-15"}))

(defcard-rg :required-picker
  "## Required Date picker"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker (merge
                     {:id        (str ::date-picker-invalid)
                      :value     (:value @data-atom)
                      :required  true
                      :on-invalid-hook #(print "invalid date: " %)
                      :label     "Please set required date"
                      :on-change (fn [x] (swap! data-atom merge {:value x}))})]))
  (r/atom {:value nil}))

(defcard-rg :date-picker-disabled
  "## Disabled Date picker"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker {:id        (str ::date-picker-disabled)
                     :value     (:value @data-atom)
                     :required  true
                     :disabled  true
                     :on-invalid-hook #(print "invalid date: " %)
                     :on-change (fn [x] (swap! data-atom merge {:value x}))}]))
  (r/atom {:value (format/unparse (format/formatter "yyyy-MM-dd") (time/to-default-time-zone (js/Date.)))}))

(defcard-rg :date-picker-with-min-and-max-date
  "## Date picker with min-date of \"2021-01-01\" and max-date - \"2025-12-31\""
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker (merge
                     {:id        (str ::date-picker-with-min-and-max-date)
                      :value     (:value @data-atom)
                      :min-date  "2021-01-01"
                      :max-date  "2025-12-31"
                      :on-invalid-hook #(print "invalid date: " %)
                      :invalid-date-message "Date should be within period from 01.01.2021 to 31.12.2025"
                      :label     "Please set required date"
                      :on-change (fn [x] (doall (print x)
                                                (swap! data-atom merge {:value x})))})]))
  (r/atom {:value nil}))

(defcard-rg :date-picker-with-disable-past
  "## Date picker with disable past"
  (fn [data-atom _]
    (apply-stiles
     [EddDatePicker (merge
                     {:id        (str ::date-picker-with-min-and-max-date)
                      :value     (:value @data-atom)
                      :disable-past true
                      :on-invalid-hook #(print "invalid date: " %)
                      :label     "Please set required date"
                      :on-change (fn [x] (doall (print x)
                                                (swap! data-atom merge {:value x})))})]))
  (r/atom {:value nil}))

(defcard-rg :date-picker-read-only
  "## Date picker read only"
  (apply-stiles
   [EddDatePicker (merge
                   {:id        (str ::date-picker-read-only)
                    :value     "2020-09-15"
                    :label     "Date"
                    :read-only true})]))

(defcard-rg :date-picker-read-only-with-underline
  "## Date picker read only with underline"
  (apply-stiles
   [EddDatePicker (merge
                   {:id        (str ::date-picker-read-only)
                    :value     "2020-09-15"
                    :label     "Date"
                    :read-only-with-underline true})]))