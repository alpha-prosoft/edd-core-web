(ns web.primitives.formatting.core
  [:require
   [clojure.string :as string]])

(defn- format
  ([value]
   (.toLocaleString value "de-DE" #js {:style "decimal" :minimumFractionDigits 0 :maximumFractionDigits 0 :useGrouping true}))
  ([value min-digits max-digits]
   (.toLocaleString value "de-DE" #js {:style "decimal" :minimumFractionDigits min-digits :maximumFractionDigits max-digits :useGrouping true})))

(defn format-number
  ([value]
   (if-not (string/blank? value)
     (format (/ value 1e8) 2 2)
     ""))
  ([value scaling]
   (case (keyword scaling)
     :mn (format (/ value 1e8) 2 2)
     :tsd (format (/ value 1e5))
     :full (format (/ value 100))
     (format (/ value 1e8) 2 2))))

(defn format-percent [value]
  (if (and (not (string/blank? value)) (number? value))
    (.toLocaleString value "de-DE" #js {:style "percent" :minimumFractionDigits 2 :maximumFractionDigits 2})
    ""))

(defn format-percent-without-percentage-sign [value]
  (if (and (not (string/blank? value)) (number? value))
    (string/replace (.toLocaleString value "de-DE" #js {:style "percent" :minimumFractionDigits 2 :maximumFractionDigits 2}) #" %" "")
    ""))