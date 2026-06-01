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
  (let [message (.-message body)
        message-type (aget body "__type")
        matched-key (get (first
                          (filter
                           (fn [{:keys [search type]}]
                             (and
                              (= type message-type)
                              (str/includes? message search)))
                           known-messages))
                         :key)]
    {:message (or matched-key message)
     :type    message-type}))

(fx/reg-fx
 :amplify-register
 (fn [{:keys [username password on-success on-failure]}]
   (let [config (get-config)]
     (-> (.fetch js/window (str "https://cognito-idp." "eu-west-1" ".amazonaws.com")
                 (clj->js {:method  "POST"
                           :body    (-> {}
                                        (assoc
                                         :ClientId (:user-pool-web-client-id config)
                                         :Username username
                                         :Password password
                                         :UserAttributes [{:Name  "email"
                                                           :Value username}])
                                        (clj->js)
                                        (#(.stringify js/JSON %)))
                           :headers {"X-Amz-Target" "AWSCognitoIdentityProviderService.SignUp"
                                     "Content-Type" "application/x-amz-json-1.1"}}))
         (.then (fn [%]
                  (let [status (.-status %)]
                    (if (> status 299)
                      (throw (ex-info status %))
                      (.json %)))))
         (.then (fn [%]
                  (rf/dispatch [:bla (js->clj % :keywordize-keys true)])
                  (rf/dispatch (conj on-success (js->clj % :keywordize-keys true)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

(fx/reg-fx
 :amplify-verify
 (fn [{:keys [username code on-success on-failure]}]
   (let [config (get-config)]
     (-> (.fetch js/window (str "https://cognito-idp." "eu-west-1" ".amazonaws.com")
                 (clj->js {:method  "POST"
                           :body    (-> {}
                                        (assoc
                                         :ClientId (:user-pool-web-client-id config)
                                         :Username username
                                         :ConfirmationCode code)
                                        (clj->js)
                                        (#(.stringify js/JSON %)))
                           :headers {"X-Amz-Target" "AWSCognitoIdentityProviderService.ConfirmSignUp"
                                     "Content-Type" "application/x-amz-json-1.1"}}))
         (.then (fn [%]
                  (let [status (.-status %)]
                    (if (> status 299)
                      (throw (ex-info status %))
                      (.json %)))))
         (.then (fn [%]
                  (rf/dispatch (conj on-success (js->clj % :keywordize-keys true)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

(fx/reg-fx
 :amplify-login
 (fn [{:keys [username password on-success on-failure]}]
   (let [config (get-config)]
     (-> (.fetch js/window (str "https://cognito-idp." "eu-west-1" ".amazonaws.com")
                 (clj->js {:method  "POST"
                           :body    (-> {}
                                        (assoc
                                         :ClientId (:user-pool-web-client-id config)
                                         :AuthFlow "USER_PASSWORD_AUTH"
                                         :AuthParameters {:USERNAME username
                                                          :PASSWORD password})
                                        (clj->js)
                                        (#(.stringify js/JSON %)))
                           :headers {"X-Amz-Target" "AWSCognitoIdentityProviderService.InitiateAuth"
                                     "Content-Type" "application/x-amz-json-1.1"}}))
         (.then (fn [%]
                  (let [status (.-status %)]
                    (if (> status 299)
                      (throw (ex-info status %))
                      (.json %)))))
         (.then (fn [%]
                   (let [response (-> %
                                      (js->clj :keywordize-keys true)
                                      (:AuthenticationResult))
                         auth {:id-token      (:IdToken response)
                               :refresh-token (:RefreshToken response)
                               :access-token  (:AccessToken response)}]
                     (store-auth! auth)
                     (rf/dispatch (conj on-success
                                        auth)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

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
                    (let [response (-> %
                                       (js->clj :keywordize-keys true)
                                       (:id_token))
                          auth {:id-token response}]
                      (rf/dispatch [::events/login-succeeded auth]))))
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
   (let [config (get-config)]
     (-> (.fetch js/window (str "https://cognito-idp." "eu-west-1" ".amazonaws.com")
                 (clj->js {:method  "POST"
                           :body    (-> {}
                                        (assoc
                                         :ClientId (:user-pool-web-client-id config)
                                         :Username username)
                                        (clj->js)
                                        (#(.stringify js/JSON %)))
                           :headers {"X-Amz-Target" "AWSCognitoIdentityProviderService.ForgotPassword"
                                     "Content-Type" "application/x-amz-json-1.1"}}))
         (.then (fn [%]
                  (let [status (.-status %)]
                    (if (> status 299)
                      (throw (ex-info status %))
                      (.json %)))))
         (.then (fn [%]
                  (rf/dispatch (conj on-success (js->clj % :keywordize-keys true)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

(fx/reg-fx
 :amplify-conform-forgot-password
 (fn [{:keys [username password code on-success on-failure]}]
   (let [config (get-config)]
     (-> (.fetch js/window (str "https://cognito-idp." "eu-west-1" ".amazonaws.com")
                 (clj->js {:method  "POST"
                           :body    (-> {}
                                        (assoc
                                         :ClientId (:user-pool-web-client-id config)
                                         :Username username
                                         :ConfirmationCode code
                                         :Password password)
                                        (clj->js)
                                        (#(.stringify js/JSON %)))
                           :headers {"X-Amz-Target" "AWSCognitoIdentityProviderService.ConfirmForgotPassword"
                                     "Content-Type" "application/x-amz-json-1.1"}}))
         (.then (fn [%]
                  (let [status (.-status %)]
                    (if (> status 299)
                      (throw (ex-info status %))
                      (.json %)))))
         (.then (fn [%]
                  (rf/dispatch (conj on-success (js->clj % :keywordize-keys true)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

(fx/reg-fx
 :amplify-resend-confirmation-code
 (fn [{:keys [username on-success on-failure]}]
   (let [config (get-config)]
     (-> (.fetch js/window (str "https://cognito-idp." "eu-west-1" ".amazonaws.com")
                 (clj->js {:method  "POST"
                           :body    (-> {}
                                        (assoc
                                         :ClientId (:user-pool-web-client-id config)
                                         :Username username)
                                        (clj->js)
                                        (#(.stringify js/JSON %)))
                           :headers {"X-Amz-Target" "AWSCognitoIdentityProviderService.ResendConfirmationCode"
                                     "Content-Type" "application/x-amz-json-1.1"}}))
         (.then (fn [%]
                  (let [status (.-status %)]
                    (if (> status 299)
                      (throw (ex-info status %))
                      (.json %)))))
         (.then (fn [%]
                  (rf/dispatch (conj on-success (js->clj % :keywordize-keys true)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

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
