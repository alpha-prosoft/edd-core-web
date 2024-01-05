(ns web.primitives.inputs.utils
  #?(:cljs (:require
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.format :as time-fmt]
            [clojure.edn :as edn]
            [clojure.string :as str]
            ["@mui/material/TextField" :default TextField]
            [cljs.pprint :as pprint]
            [goog.string :as gstring])
     :clj  (:require
            [clojure.string :as str])))

#?(:cljs (def adapted-text-field (r/adapt-react-class TextField)))

(defn translate-BMK-in-numbers [value]
  (let [character (last (filter #(#{\b, \B, \m, \M, \k, \K} %) value))
        numbers (apply str (filter #(#{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9} %) value))
        factor (case (str/upper-case (if character character ""))
                 \B 1000000000
                 \M 1000000
                 \K 1000
                 1)]
    (if (str/blank? value) "" (* numbers factor))))

(defn handle-comma [value]
  (let [decimal? (str/includes? value ",")
        split (str/split value ",")
        second (second split)
        val (if decimal?
              (str (first split) "." (when-not (nil? second) (subs second 0 2)))
              value)]
    (apply str (filter #(#{\. \0 \1 \2 \3 \4 \5 \6 \7 \8 \9} %) val))))

#?(:cljs (defn transform-percent [value]
           (let [percent (handle-comma value)
                 percent-decimal (gstring/format "%.4f" (/ (edn/read-string percent) 100))
                 percent-float (js/parseFloat percent-decimal)]
             (if (str/blank? value) "" percent-float))))

(defn handle-input-change
  [event callback transform-func]
  (let [target (.-target event)
        value (.-value target)
        changed-value (transform-func value)]
    (callback changed-value)
    (set! (.-value target) changed-value)))

#?(:cljs (defn handle-input-change-with-number-formatting
           [event callback formatting-func separator]
           (let [target (.-target event)
                 value (.-value target)
                 changed-value (translate-BMK-in-numbers value)
                 amount-cents (* changed-value 100)
                 formatted-value (if (some? formatting-func)
                                   (formatting-func changed-value)
                                   (if (some? separator)
                                     (pprint/cl-format nil (str "~,,'" separator ":D") changed-value)
                                     (pprint/cl-format nil "~,,'.:D" changed-value)))]
             (callback amount-cents)
             (set! (.-value target) formatted-value))))

#?(:cljs (defn handle-input-change-with-percent-formatting
           [event callback]
           (let [target (.-target event)
                 value (.-value target)
                 changed-value (transform-percent value)
                 formatted-value (str/replace (handle-comma value) #"\." ",")]
             (callback changed-value)
             (set! (.-value target) formatted-value))))

(defn handle-switcher-change [{:keys [left-value right-value value callback]}]
  (let [new-value (if (= value left-value) right-value left-value)]
    (callback new-value)))

#?(:cljs
   (defn jsx->clj
     [x]
     (into {} (for [k (.keys js/Object x)]
                [k (aget x k)]))))

#?(:cljs
   (def date-formatter (time-fmt/formatter "yyyy-MM-dd")))

#?(:cljs
   (defn date-string->ui-view [date-string]
     (try
       (time-fmt/unparse (time-fmt/formatter "dd.MM.yyyy") (time-fmt/parse date-formatter date-string))
       (catch js/Error _ ""))))

#?(:cljs
   (defn date? [date]
     (try
       (.toISOString date)
       (catch js/Error _
         false))))

#?(:cljs
   (defn validate-date-min-max-date [{:keys [component-min-date component-max-date disablePast disable-past
                                             set-date-input-invalid on-change]} date]
     (let [date-invalid? (and
                          (some? date)
                          (or (t/before? date (time-fmt/parse-local-date component-min-date))
                              (t/after? date (time-fmt/parse-local-date component-max-date))
                              (and (or disablePast disable-past) (t/before? date (t/today)))))]
       (set-date-input-invalid date-invalid?)
       (when (not date-invalid?)
         (on-change (time-fmt/unparse date-formatter date))))))

#?(:cljs (defn handle-date-picker-date-change
           [{:keys [set-focused on-change date required set-date-input-invalid]
             :or   {on-change #(print "date: " %)}
             :as   props}]
           (doall
            (set-focused true)
            (cond
              (nil? date) (doall (when required (set-date-input-invalid true)) (on-change nil))
              (false? (date? date)) (doall (set-date-input-invalid true) nil)
              :else (validate-date-min-max-date props (t/to-default-time-zone date))))))

(defn invalid-date? [{:keys [value required date-input-invalid? touched? focused?]
                      :or   {required false}}]
  (and
   touched?
   (not focused?)
   (or
    date-input-invalid?
    (and required (str/blank? value)))))

(defn clean-switch-props [props]
  (->
   props
   (dissoc :left-label)
   (dissoc :right-label)
   (dissoc :left-value)
   (dissoc :right-value)
   (dissoc :label-placement)
   (dissoc :value)))

#?(:clj (defn string-to-float [value]
          (read-string value)))