(ns web.widgets.login.core
  (:require [re-frame.fx :as fx]
            [clojure.string :as str]
            [web.widgets.login.events :as events]
            [web.widgets.login.db :as db]
            [web.widgets.login.utils :as utils]
            [re-frame.core :as rf]
            [edd.db :as edd-db]))

(defn store-auth!
  "Persist auth tokens to localStorage under the \"auth\" key."
  [auth]
  (-> js/window
      (.-localStorage)
      (.setItem "auth" (.stringify js/JSON (clj->js auth)))))

(defn valid-id-token?
  "True when the id-token is a structurally valid, decodable JWT. Expiry is
   not checked here — an expired but well-formed token still decodes and is
   handled by the refresh flow."
  [id-token]
  (some? (utils/decode-token-claims id-token)))

(defn- restore-auth-from-query!
  "If the URL contains an ?id_token=... query param, store it as auth in
   localStorage and remove the param from the URL via history.replaceState.
   The token is only stored when it is a decodable JWT, so a malformed value
   in the URL is dropped instead of poisoning storage.
   Blocking: completes synchronously before init proceeds."
  []
  (let [location  (.-location js/window)
        url       (js/URL. (.-href location))
        params    (.-searchParams url)
        id-token  (.get params "id_token")]
    (when (and id-token (seq id-token))
      (when (valid-id-token? id-token)
        (store-auth! {:id-token id-token}))
      (.delete params "id_token")
      (-> js/window
          (.-history)
          (.replaceState nil "" (.toString url))))))

(defn discard-invalid-stored-auth!
  "Clears the stored auth blob when it is corrupt or carries an id-token that
   is not a decodable JWT, so a malformed token can never be hydrated into the
   app and crash the UI. A missing id-token or a structurally valid (incl.
   expired) one is left untouched."
  []
  (try
    (let [storage  (.-localStorage js/window)
          raw      (.getItem storage "auth")
          parsed   (when (and raw (seq raw))
                     (js->clj (.parse js/JSON raw) :keywordize-keys true))
          id-token (:id-token parsed)]
      (when (and (some? id-token)
                 (not (valid-id-token? id-token)))
        (.removeItem storage "auth")))
    (catch :default _
      (-> js/window (.-localStorage) (.removeItem "auth")))))

(defn init
  [{:keys [config]}]
  (restore-auth-from-query!)
  (discard-invalid-stored-auth!)
  (rf/dispatch [::events/initialize-db]))

(defn get-config
  []
  (let [config (js->clj (.-eddconfig js/window) :keywordize-keys true)
        oauth {:userPoolId              (get config :AuthUserPoolId)
               :domain                  (get config :AuthUserPoolDomain)
               :scope                   ["email" "openid"]
               :redirectSignIn          "http://localhost:3000/"
               :responseType            "code"
               :user-pool-web-client-id (get config :AuthUserPoolClientId)
               :region                  (get config :Region "eu-central-1")
               :authenticationFlowType  "USER_PASSWORD_AUTH"}]
    oauth))

(def known-messages
  [{:type    "InvalidParameterException"
    :message "2 validation errors detected: Value at 'password' failed to satisfy constraint: Member must satisfy regular expression pattern: ^[\\S]+.*[\\S]+$; Value at 'password' failed to satisfy constraint: Member must have length greater than or equal to 6"
    :search  "Value at 'password' failed to satisfy constraint"
    :key     :invalid-password}
   {:type    "InvalidPasswordException"
    :message "Password did not conform with policy: Password must have uppercase characters"
    :search  "Password did not conform with policy"
    :key     :invalid-password}
   {:type    "InvalidPasswordException"
    :message "Password did not conform with policy: Password must have numeric characters"
    :search  "Password did not conform with policy"
    :key     :invalid-password}
   {:type    "InvalidParameterException"
    :message "1 validation error detected: Value at 'password' failed to satisfy constraint: Member must have length greater than or equal to 6"
    :search  "Value at 'password' failed to satisfy constraint"
    :key     :invalid-password}
   {:type    "UsernameExistsException"
    :message "User already exists"
    :search  "User already exists"
    :key     :user-exists}
   {:type    "InvalidParameterException"
    :message "Invalid email address format."
    :search  "Invalid email address format."
    :key     :invalid-email}
   {:message "Incorrect username or password."
    :search  "Incorrect username or password."
    :type    "NotAuthorizedException"
    :key     :invalid-credentials}
   {:message "User does not exist."
    :search  "User does not exist."
    :type    "UserNotFoundException"
    :key     :invalid-credentials}
   {:message "Invalid code provided, please request a code again."
    :type    "ExpiredCodeException"
    :key     :code-expired
    :search  "Invalid code provided, please request a code again."}
   {:message "Cannot reset password for the user as there is no registered/verified email or phone_number"
    :type    "InvalidParameterException"
    :search  "Cannot reset password for the user as there is no registered/verified email"
    :key     :account-not-confirmed}
   {:message "Attempt limit exceeded, please try after some time."
    :type    "LimitExceededException"
    :search  "Attempt limit exceeded, please try after some time."
    :key     :attempt-limit-exceeded}
   {:message "Invalid verification code provided, please try again."
    :type    "CodeMismatchException"
    :search  "Invalid verification code provided"
    :key     :invalid-code}
   {:message "Missing required parameter USERNAME"
    :type    "InvalidParameterException"
    :search  "Missing required parameter USERNAME"
    :key     :missing-username}
   {:message "Missing required parameter PASSWORD"
    :type    "InvalidParameterException"
    :search  "Missing required parameter PASSWORD"
    :key     :missing-password}])

(defn match-error-message
  [body]
  (let [message
        (.-message body)

        message-type
        (aget body "__type")]
    {:message (utils/cognito-error-key message-type message)
     :type    message-type}))

(def network-error
  {:message :network-error
   :type    "NetworkError"})

(def ^:private cognito-url
  (str "https://cognito-idp." "eu-west-1" ".amazonaws.com"))

(defn- refused!
  [on-failure response]
  (let [parsed
        (.then (.json response)
               (fn [body]
                 (rf/dispatch (conj on-failure (match-error-message body)))))]
    (.catch parsed
            (fn [_]
              (rf/dispatch (conj on-failure {:message :unknown-error
                                             :type    (str (.-status response))}))))))

(defn- cognito-call!
  "Sends `target` with `body` to Cognito. An answer goes to `on-answer`, a refusal to `on-failure` as a
   translation key, and a request that never reached Cognito to `on-failure` as `network-error`."
  [target body on-failure on-answer]
  (let [request
        (clj->js {:method  "POST"
                  :body    (.stringify js/JSON (clj->js body))
                  :headers {"X-Amz-Target" (str "AWSCognitoIdentityProviderService." target)
                            "Content-Type" "application/x-amz-json-1.1"}})]
    (.then (.fetch js/window cognito-url request)
           (fn [response]
             (if (.-ok response)
               (.then (.json response) on-answer)
               (refused! on-failure response)))
           (fn [_]
             (rf/dispatch (conj on-failure network-error))))))

(defn- answer-to
  [on-success]
  (fn [answer]
    (rf/dispatch (conj on-success (js->clj answer :keywordize-keys true)))))

(defn- client-id
  []
  (:user-pool-web-client-id (get-config)))

(fx/reg-fx
 :amplify-register
 (fn [{:keys [username password on-success on-failure]}]
   (let [email
         (utils/sent-email username)]
     (cognito-call! "SignUp"
                    {:ClientId       (client-id)
                     :Username       email
                     :Password       password
                     :UserAttributes [{:Name  "email"
                                       :Value email}]}
                    on-failure
                    (answer-to on-success)))))

(fx/reg-fx
 :amplify-verify
 (fn [{:keys [username code on-success on-failure]}]
   (cognito-call! "ConfirmSignUp"
                  {:ClientId         (client-id)
                   :Username         (utils/sent-email username)
                   :ConfirmationCode (str/trim (str code))}
                  on-failure
                  (answer-to on-success))))

(defn- authenticated!
  [on-success on-failure answer]
  (let [response
        (:AuthenticationResult (js->clj answer :keywordize-keys true))

        id-token
        (:IdToken response)]
    (if (valid-id-token? id-token)
      (let [auth
            {:id-token      id-token
             :refresh-token (:RefreshToken response)
             :access-token  (:AccessToken response)}]
        (store-auth! auth)
        (rf/dispatch (conj on-success auth)))
      (rf/dispatch (conj on-failure
                         {:message :unknown-error
                          :type    "NoAuthenticationResult"})))))

(fx/reg-fx
 :amplify-login
 (fn [{:keys [username password on-success on-failure]}]
   (cognito-call! "InitiateAuth"
                  {:ClientId       (client-id)
                   :AuthFlow       "USER_PASSWORD_AUTH"
                   :AuthParameters {:USERNAME (utils/sent-email username)
                                    :PASSWORD password}}
                  on-failure
                  #(authenticated! on-success on-failure %))))

(defn auth []
  (try
    (let [auth-string (-> js/window
                          (.-localStorage)
                          (.getItem "auth"))]
      (when (and auth-string (seq auth-string))
        (-> (.parse js/JSON auth-string)
            (js->clj :keywordize-keys true))))
    (catch :default _ nil)))

(defn amplify-refresh-credentials
  ([{:keys [do-post-with-retry post-for attempt body-str] :as interrupted-call}]
   (let [config (get-config)
         refresh-token (:refresh-token (auth))]

     (if refresh-token
       (-> (.fetch js/window (str "https://" (:domain config) "/oauth2/token")
                   (clj->js {:method  "POST"
                             :headers {"Content-Type" "application/x-www-form-urlencoded"}
                             :body    (str
                                       "grant_type=refresh_token&"
                                       "client_id=" (:user-pool-web-client-id config) "&"
                                       "refresh_token=" refresh-token)}))
           (.then (fn [%]
                    (let [status (.-status %)]
                      (if (> status 299)
                        (-> (.text %)
                            (.then (fn []
                                     (doall
                                      (-> js/window
                                          (.-localStorage)
                                          (.setItem "auth" "{}"))
                                      (rf/dispatch [::events/open-dialog :login])))))
                        (.json %)))))
           (.then (fn [%]
                    (let [id-token (-> %
                                       (js->clj :keywordize-keys true)
                                       (:id_token))]
                      (if (valid-id-token? id-token)
                        (rf/dispatch [::events/login-succeeded {:id-token id-token}])
                        (do
                          (-> js/window
                              (.-localStorage)
                              (.setItem "auth" "{}"))
                          (rf/dispatch [::events/open-dialog :login]))))))
           (.then (when (some? interrupted-call)
                    (do-post-with-retry post-for attempt body-str))))
       (rf/dispatch [::events/open-dialog :login]))))
  ([] (amplify-refresh-credentials nil)))

(fx/reg-fx
 :amplify-refresh-credentials
 (fn []
   (amplify-refresh-credentials)))

(fx/reg-fx
 :amplify-forgot-password
 (fn [{:keys [username on-success on-failure]}]
   (cognito-call! "ForgotPassword"
                  {:ClientId (client-id)
                   :Username (utils/sent-email username)}
                  on-failure
                  (answer-to on-success))))

(fx/reg-fx
 :amplify-conform-forgot-password
 (fn [{:keys [username password code on-success on-failure]}]
   (cognito-call! "ConfirmForgotPassword"
                  {:ClientId         (client-id)
                   :Username         (utils/sent-email username)
                   :ConfirmationCode (str/trim (str code))
                   :Password         password}
                  on-failure
                  (answer-to on-success))))

(fx/reg-fx
 :amplify-resend-confirmation-code
 (fn [{:keys [username on-success on-failure]}]
   (cognito-call! "ResendConfirmationCode"
                  {:ClientId (client-id)
                   :Username (utils/sent-email username)}
                  on-failure
                  (answer-to on-success))))

(fx/reg-fx
 :amplify-logout
 (fn []
   (-> js/window
       (.-localStorage)
       (.setItem "auth" "{}"))))

(def ensure-credentials
  (rf/->interceptor
   :id     :ensure-credentials
   :before (fn [context]
             (let [user (get-in context [:coeffects :db ::edd-db/user])]
               (if user
                 (update-in context [:coeffects :db] #(dissoc % ::db/interrupted-event))
                 (-> context
                     (assoc-in  [:coeffects :db ::db/interrupted-event]
                                (get-in context [:coeffects :event]))))))
   :after (fn [context]
            (let [user (get-in context [:coeffects :db ::edd-db/user])
                  coeffects-db (get-in context [:coeffects :db])
                  context (if user
                            context
                            (assoc-in context [:effects] {:db coeffects-db
                                                          :amplify-refresh-credentials []}))]
              context))))
