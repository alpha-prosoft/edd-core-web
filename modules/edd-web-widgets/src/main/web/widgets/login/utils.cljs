(ns web.widgets.login.utils
  (:require
   [clojure.walk :refer [postwalk]]
   [ajax.json :as ajax-json]
   [clojure.string :as str]))

(defn parse-fields [e]
  (postwalk (fn [x]
              (cond
                (and (string? x)
                     (str/starts-with? x ":")) (keyword (subs x 1))
                (and (string? x)
                     (str/starts-with? x "#")) (uuid (subs x 1))
                :else x))
            e))

(defn json-parser [& params]
  (-> (apply ajax-json/read-json-native params)
      (parse-fields)))
