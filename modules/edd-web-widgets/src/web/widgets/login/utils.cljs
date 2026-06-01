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

(defn decode-token-claims [id-token]
  (when-not (str/blank? id-token)
    (try
      (let [payload
            (second (str/split id-token #"\."))]
        (when-not (str/blank? payload)
          (json-parser false true (b64/decodeString payload))))
      (catch :default _ nil))))

(defn decode-user-name [id-token]
  (:email (decode-token-claims id-token)))

(defn decode-user-id [id-token]
  (let [claims
        (decode-token-claims id-token)]
    (or (:custom:user-id claims)
        (:user-id claims)
        (:sub claims))))
