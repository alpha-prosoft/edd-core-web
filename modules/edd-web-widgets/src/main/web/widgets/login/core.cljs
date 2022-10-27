(ns web.widgets.login.core
  (:require [re-frame.fx :as fx]
            [clojure.string :as str]
            [web.widgets.login.events :as events]
            [re-frame.core :as rf]))

(defn init
  []
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
        message-type (aget body "__type")]
    {:message (get (first
                    (filter
                     (fn [{:keys [search type]}]
                       (and
                        (= type message-type)
                        (str/includes? message search)))
                     known-messages))
                   :key)
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
                              :access-token  (:AccessToken response)}
                        auth-string (.stringify js/JSON (clj->js auth))]
                    (-> js/window
                        (.-localStorage)
                        (.setItem "auth" auth-string))
                    (rf/dispatch (conj on-success
                                       auth)))))
         (.catch (fn [e]
                   (-> (ex-data e)
                       (.json)
                       (.then (fn [body]
                                (rf/dispatch (conj on-failure
                                                   (match-error-message body))))))))))))

(fx/reg-fx
 :amplify-refresh-credentials
 (fn [{:keys [on-success]}]
   (let [config (get-config)
         auth-string (-> js/window
                         (.-localStorage)
                         (.getItem "auth"))
         auth (-> (.parse js/JSON auth-string)
                  (js->clj :keywordize-keys true))
         refresh-token (:refresh-token auth)]

     (when refresh-token
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
                                     (-> js/window
                                         (.-localStorage)
                                         (.setItem "auth" "{}")))))
                        (.json %)))))
           (.then (fn [%]
                    (let [response (-> %
                                       (js->clj :keywordize-keys true)
                                       (:id_token))
                          auth {:id-token response}]
                      (rf/dispatch (conj on-success
                                         auth))))))))))

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
