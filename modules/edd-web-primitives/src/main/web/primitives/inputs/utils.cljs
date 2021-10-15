(ns web.primitives.inputs.utils
  (:require [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.coerce :as t-coerce]
            [cljs-time.format :as time-fmt]
            ["@mui/material/TextField" :default TextField]
            [clojure.string :as string]
            [cljs.pprint :as pprint]))

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

(def timestamp-server-formatter (time-fmt/formatter "yyyy-MM-dd"))

(defn date-time->date-string [date-time]
  (if date-time
    (time-fmt/unparse timestamp-server-formatter (t-coerce/from-date date-time))
    date-time))

(defn date?
  ([date]
   (try
     (t/date? (time-fmt/parse date))
     (catch js/Error e
       false))))

(defn invalid-date? [touched? focused? {:keys [value required]
                                        :or   {required false}}]
  (and touched? (not focused?) (if required
                                 (not (date? value))
                                 (if (nil? value)
                                   false
                                   (not (date? value))))))

(defn clean-switch-props [props]
  (->
   props
   (dissoc :left-label)
   (dissoc :right-label)
   (dissoc :left-value)
   (dissoc :right-value)
   (dissoc :label-placement)
   (dissoc :value)))
