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

(defn password-weak? [password]
  (boolean (some true? (vals (validate-password (str password))))))

(defn sent-email
  "The email address as it goes to Cognito: what was typed, trimmed and lower-cased."
  [username]
  (some-> username str str/trim str/lower-case))

(def ^:private too-many-tries
  #{"LimitExceededException" "TooManyRequestsException" "TooManyFailedAttemptsException"})

(defn cognito-error-key
  "The translation key for a refusal of Cognito (`type` is its `__type`), never Cognito's own sentence."
  [type message]
  (let [message
        (str message)

        about-password?
        (str/includes? message "'password'")]
    (cond
      (re-find #"(?i)email limit" message)                       :email-limit-exceeded
      (re-find #"(?i)password attempts exceeded" message)        :attempt-limit-exceeded
      (contains? too-many-tries type)                            :attempt-limit-exceeded
      (= "NotAuthorizedException" type)                          :invalid-credentials
      (= "UserNotFoundException" type)                           :invalid-credentials
      (= "CodeMismatchException" type)                           :invalid-code
      (= "ExpiredCodeException" type)                            :code-expired
      (= "InvalidPasswordException" type)                        :invalid-password
      (= "UsernameExistsException" type)                         :user-exists
      (and (= "InvalidParameterException" type) about-password?) :invalid-password
      (re-find #"no registered/verified email" message)          :account-not-confirmed
      (re-find #"(?i)already confirmed" message)                 :already-confirmed
      (re-find #"Invalid email address format" message)          :invalid-email
      (re-find #"Missing required parameter USERNAME" message)   :missing-username
      (re-find #"Missing required parameter PASSWORD" message)   :missing-password
      :else                                                      :unknown-error)))

(def email-forms
  #{:login :register :forgot-password})

(def code-forms
  #{:confirm-login :confirm-password-reset})

(def new-password-forms
  #{:register :confirm-password-reset})

(defn submit-problem
  "What keeps the dialog's form from being sent, judged on the values it holds when it is sent; nil when nothing does."
  [form-type {:keys [username password code]}]
  (cond
    (and (email-forms form-type) (nil? (validate-email (sent-email username))))
    :enter-email

    (and (= :login form-type) (str/blank? password))
    :enter-password

    (and (code-forms form-type) (str/blank? code))
    :enter-code

    (and (new-password-forms form-type) (password-weak? password))
    :invalid-password

    :else
    nil))

(defn error-field
  "The field an error of the dialog is about: it is linked to the error and gets the focus."
  [form-type error]
  (cond
    (#{:enter-email :invalid-email :missing-username :user-exists} error)
    :username

    (#{:enter-code :invalid-code :code-expired} error)
    :code

    (#{:enter-password :invalid-password :invalid-credentials :missing-password} error)
    :password

    (#{:login :register} form-type)
    :password

    (= :forgot-password form-type)
    :username

    :else
    :code))

(defn start-field
  "The field that has the focus when a form opens: the password when the email is already filled in."
  [form-type username]
  (cond
    (code-forms form-type)
    :code

    (and (#{:login :register} form-type) (not (str/blank? username)))
    :password

    (email-forms form-type)
    :username

    :else
    :password))

(defn pool-region
  "The AWS region of a Cognito user pool id (`eu-west-1_AbC123` -> \"eu-west-1\")."
  [pool-id]
  (second (re-matches #"([a-z]{2}(?:-[a-z]+)+-\d+)_.+" (str pool-id))))

(def default-retry-delays
  [1000 3000])

(defn retry-delay
  "Milliseconds to wait before sending again a request that did not reach Cognito after `attempt` retries; nil when it
   is not sent again."
  [delays attempt]
  (get (vec delays) attempt))

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
