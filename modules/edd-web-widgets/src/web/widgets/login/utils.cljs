(ns web.widgets.login.utils
  (:require
   [clojure.walk :refer [postwalk]]
   [ajax.json :as ajax-json]
   [goog.crypt.base64 :as b64]
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

(defn validate-email [email]
  (re-matches #".+\@.+\..+" (str email)))

(defn validate-password [password]
  {:missing-upper-case? (nil? (re-seq #"[A-Z]" password))
   :missing-lower-case? (nil? (re-seq #"[a-z]" password))
   :missing-number?     (nil? (re-seq #"[0-9]" password))
   :missing-length-8?   (< (count password) 8)})

(defn decode-user-name [id-token]
  (let [decoded (b64/decodeString
                  (second
                    (clojure.string/split id-token #"\.")))]
    (when (some? id-token)
      (-> (json-parser false true decoded)
          (:email)))))
