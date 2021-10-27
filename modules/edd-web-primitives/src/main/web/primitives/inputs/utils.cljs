(ns web.primitives.inputs.utils
  (:require [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.format :as time-fmt]
            ["@mui/material/TextField" :default TextField]))

(def adapted-text-field (r/adapt-react-class TextField))

(defn handle-input-change
  [event callback transform-func]
  (let [target (.-target event)
        value (.-value target)
        changed-value (transform-func value)]
    (callback changed-value)
    (set! (.-value target) changed-value)))

(defn handle-switcher-change [{:keys [left-value right-value value callback]}]
  (let [new-value (if (= value left-value) right-value left-value)]
    (callback new-value)))

(defn jsx->clj
  [x]
  (into {} (for [k (.keys js/Object x)]
             [k (aget x k)])))

(def date-formatter (time-fmt/formatter "yyyy-MM-dd"))

(defn date-string->ui-view [date-string]
  (try
    (time-fmt/unparse (time-fmt/formatter "dd.MM.yyyy") (time-fmt/parse date-formatter date-string))
    (catch js/Error _ "")))

(defn date? [date]
  (try
    (.toISOString date)
    (catch js/Error _
      false)))

(defn validate-date-min-max-date [{:keys [component-min-date component-max-date disablePast disable-past
                                          set-date-input-invalid]} date-string]
  (let [date (time-fmt/parse date-string)
        invalid? (and (some? date-string)
                      (or (t/before? date (time-fmt/parse component-min-date))
                          (t/after? date (time-fmt/parse component-max-date))
                          (and (or disablePast disable-past) (t/before? date (t/today-at-midnight)))))]
    (set-date-input-invalid invalid?)
    date-string))

(defn date->string [{:keys [date required set-date-input-invalid] :as props}]
  (cond
    (nil? date) (doall (when required (set-date-input-invalid true)) nil)
    (false? (date? date)) (doall (set-date-input-invalid true) nil)
    :else (validate-date-min-max-date props
                                      (time-fmt/unparse date-formatter
                                                        (t/to-default-time-zone date)))))

(defn handle-date-picker-date-change
  [{:keys [set-focused on-change]
    :or   {on-change #(print "date: " %)}
    :as   props}]
  (doall
   (set-focused true)
   (on-change
    (date->string props))))

(defn invalid-date? [{:keys [value required date-input-invalid? touched? focused?]
                      :or   {required false}}]
  (and
   touched?
   (not focused?)
   (or
    date-input-invalid?
    (and required (nil? value)))))

(defn clean-switch-props [props]
  (->
   props
   (dissoc :left-label)
   (dissoc :right-label)
   (dissoc :left-value)
   (dissoc :right-value)
   (dissoc :label-placement)
   (dissoc :value)))
