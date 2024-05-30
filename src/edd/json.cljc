(ns edd.json
  (:require [clojure.walk :refer [postwalk]]
            [clojure.string :as str]
            [ajax.json :as ajax-json]))

(defn read-uuid [val]
  #?(:cljs (uuid val)
     :clj  (java.util.UUID/fromString val)))

(defn decode-json-special
  [in]
  (let [v (name in)]
    (case (first v)
      \: (if (str/starts-with? v "::")
           (subs v 1)
           (keyword (subs v 1)))
      \# (if (str/starts-with? v "##")
           (subs v 1)
           (parse-uuid (subs v 1)))
      in)))

(defn parse-custom-fields
  [edn]
  (postwalk (fn [x]
              (cond
                (keyword? x)
                (decode-json-special x)

                (and (string? x)
                     (str/starts-with? x ":"))
                (keyword (subs x 1))

                (and (string? x)
                     (str/starts-with? x "#"))
                (read-uuid (subs x 1))

                :else x))
            edn))

(defn fmap [f m]
  (into (empty m)
        (if (map? m)
          (for [[k v] m]
            [k (f v)])
          (for [k m]
            (f k)))))

(defn convert
  [x]
  (cond
    (keyword? x) (str ":" (name x))
    (uuid? x) (str "#" x)
    (coll? x) (fmap convert x)
    :else x))

(defn encode-custom-fields
  [edn]
  (fmap convert edn))

(defn read-json [raw keywords? text]
  #?(:clj  (ajax-json/read-json-cheshire raw keywords? (char-array text))
     :cljs (ajax-json/read-json-native raw keywords? text)))

(defn custom-json-parser
  [& params]
  (-> (apply read-json params)
      (parse-custom-fields)))

(def custom-response-format
  (ajax-json/make-json-response-format
   custom-json-parser))
