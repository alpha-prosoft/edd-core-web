(ns web.widgets.gallery.it.login-test
  "The login dialog against a Cognito answered by routes: the real pool is never called."
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.button :as button]
   [web.primitives.sdk.core :as sdk]
   [web.primitives.sdk.dialog :as dialog]
   [web.primitives.sdk.text-field :as text-field])
  (:import
   [com.microsoft.playwright Locator Locator$PressSequentiallyOptions Page$GetByRoleOptions Route Route$FulfillOptions]
   [com.microsoft.playwright.options AriaRole]
   [java.util Base64]
   [java.util.function Consumer]))

(def ^:private dialog-el
  [:login-bar :dialog])

(defn- id-token
  [email]
  (let [part
        (fn [m]
          (.encodeToString (Base64/getEncoder) (.getBytes ^String (json/generate-string m) "UTF-8")))]
    (str (part {:alg "none"}) "." (part {:email email :sub "user-1"}) ".signature")))

(defn- answer
  [status body]
  (-> (Route$FulfillOptions.)
      (.setStatus (int status))
      (.setContentType "application/x-amz-json-1.1")
      (.setBody (json/generate-string body))))

(defn- refusal
  [type message]
  [400 {:__type type :message message}])

(defn- answer-cognito!
  "Routes every Cognito request to `respond` (target, parsed body -> [status body], :abort or :hold) and returns an
   atom with the requests seen and the routes held back."
  [respond]
  (let [seen
        (atom {:requests [] :held []})]
    (.route (sdk/page) "https://cognito-idp.**"
            (reify Consumer
              (accept [_ route]
                (let [request
                      (.request ^Route route)

                      target
                      (str/replace (str (get (.headers request) "x-amz-target")) "AWSCognitoIdentityProviderService." "")

                      body
                      (json/parse-string (.postData request) true)

                      reply
                      (respond target body)]
                  (swap! seen update :requests conj {:target target :body body})
                  (cond
                    (= :abort reply) (.abort ^Route route)
                    (= :hold reply)  (swap! seen update :held conj route)
                    :else            (.fulfill ^Route route (answer (first reply) (second reply))))))))
    seen))

(defn- targets
  [seen]
  (mapv :target (:requests @seen)))

(defn- focused-id
  []
  (str (.evaluate (sdk/page) "() => document.activeElement && (document.activeElement.id || document.activeElement.getAttribute('data-testid')) || ''")))

(defn- focus-becomes?
  [id]
  (sdk/eventually #(= id (focused-id))))

(defn- open-login!
  []
  (h/open-page! :login)
  (button/click! [:login-bar :login])
  (dialog/wait-open! dialog-el))

(defn- type-fast!
  [el-id text]
  (let [field
        ^Locator (sdk/el el-id)]
    (.click field)
    (.pressSequentially field text (-> (Locator$PressSequentiallyOptions.) (.setDelay 5.0)))))

(defn- dialog-text
  []
  (dialog/body-text dialog-el))

(defn- error-text
  []
  (sdk/text [:login-bar :error-message]))

(deftest the-dialog-is-a-named-form-with-named-fields
  (with-gallery
    (step "open the login dialog" open-login!)

    (step "the dialog is named by its title, the fields have names, autocomplete and labels"
          (fn []
            (is
             (= 1
                (.count (.getByRole (sdk/page) AriaRole/DIALOG (-> (Page$GetByRoleOptions.) (.setName "Log in"))))))

            (is
             (= ["email" "username" "password" "current-password"]
                [(sdk/attr [:login-bar :username] "name")
                 (sdk/attr [:login-bar :username] "autocomplete")
                 (sdk/attr [:login-bar :password] "name")
                 (sdk/attr [:login-bar :password] "autocomplete")]))

            (is
             (= "Email address"
                (text-field/label [:login-bar :username])))

            (is
             (= "login-bar--username"
                (focused-id)))))

    (step "the password toggle has a name and says whether it is pressed"
          (fn []
            (is
             (= ["Show password" "false" "password"]
                [(sdk/attr [:login-bar :password-visibility] "aria-label")
                 (sdk/attr [:login-bar :password-visibility] "aria-pressed")
                 (sdk/attr [:login-bar :password] "type")]))

            (button/click! [:login-bar :password-visibility])

            (is
             (sdk/eventually= ["Hide password" "true" "text"]
                              #(vector (sdk/attr [:login-bar :password-visibility] "aria-label")
                                       (sdk/attr [:login-bar :password-visibility] "aria-pressed")
                                       (sdk/attr [:login-bar :password] "type"))))))

    (step "the reset step asks for a one-time code and a new password"
          (fn []
            (button/click! [:login-bar :close])
            (dialog/wait-closed! dialog-el)
            (button/click! [:login :states :reset-code])
            (dialog/wait-open! dialog-el)

            (is
             (= ["code" "one-time-code" "new-password"]
                [(sdk/attr [:login-bar :confirmation-code] "name")
                 (sdk/attr [:login-bar :confirmation-code] "autocomplete")
                 (sdk/attr [:login-bar :password] "autocomplete")]))))))

(deftest fast-typing-loses-no-character
  (with-gallery
    (step "open the login dialog" open-login!)

    (step "30 characters 5 ms apart reach the email and the password field whole"
          (fn []
            (let [email
                  "Fast.Typist.0123@Example.co.uk"

                  password
                  "Typed-Fast-Password-1234567890"]
              (type-fast! [:login-bar :username] email)
              (type-fast! [:login-bar :password] password)

              (is
               (= [30 30]
                  [(count email) (count password)]))

              (is
               (= [email password]
                  [(text-field/value [:login-bar :username])
                   (text-field/value [:login-bar :password])])))))

    (step "and the code field of the reset step"
          (fn []
            (button/click! [:login-bar :close])
            (dialog/wait-closed! dialog-el)
            (button/click! [:login :states :reset-code])
            (dialog/wait-open! dialog-el)
            (type-fast! [:login-bar :confirmation-code] "123456789012345678901234567890")

            (is
             (= "123456789012345678901234567890"
                (text-field/value [:login-bar :confirmation-code])))))))

(deftest enter-right-after-fill-logs-in-and-the-email-is-remembered
  (with-gallery
    (let [seen
          (answer-cognito! (fn [target {:keys [AuthParameters]}]
                             (if (= "InitiateAuth" target)
                               [200 {:AuthenticationResult {:IdToken      (id-token (:USERNAME AuthParameters))
                                                            :RefreshToken "refresh"
                                                            :AccessToken  "access"}}]
                               (refusal "InvalidParameterException" "unexpected"))))]
      (step "open the login dialog" open-login!)

      (step "filled as by a browser and Enter at once: the form goes out with what the fields hold"
            (fn []
              (.fill (sdk/el [:login-bar :username]) " Someone@Example.com ")
              (.fill (sdk/el [:login-bar :password]) "Passw0rd-x")
              (.press (sdk/el [:login-bar :password]) "Enter")
              (dialog/wait-closed! dialog-el)

              (is
               (= ["InitiateAuth"]
                  (targets seen)))

              (is
               (= {:USERNAME "someone@example.com" :PASSWORD "Passw0rd-x"}
                  (get-in @seen [:requests 0 :body :AuthParameters])))

              (is
               (sdk/becomes-visible? [:login-bar :logout]))))

      (step "after logging out the dialog offers the email address that logged in"
            (fn []
              (button/click! [:login-bar :logout])
              (button/click! [:login-bar :login])
              (dialog/wait-open! dialog-el)

              (is
               (= "someone@example.com"
                  (text-field/value [:login-bar :username]))))))))

(deftest a-refused-login-names-no-cause-and-focuses-the-password
  (with-gallery
    (answer-cognito! (fn [_ {:keys [AuthParameters]}]
                       (if (= "known@example.com" (:USERNAME AuthParameters))
                         (refusal "NotAuthorizedException" "Incorrect username or password.")
                         (refusal "UserNotFoundException" "User does not exist."))))
    (step "open the login dialog" open-login!)

    (doseq [email ["known@example.com" "unknown@example.com"]]
      (step (str "a wrong password for " email ": a plain text, linked to the focused password field")
            (fn []
              (text-field/fill! [:login-bar :username] email)
              (text-field/fill! [:login-bar :password] "Wrong-password-1")
              (.press (sdk/el [:login-bar :username]) "Enter")

              (is
               (sdk/eventually= "Wrong email address or password."
                                #(when (sdk/present? [:login-bar :error-message]) (error-text))))

              (is
               (= "alert"
                  (sdk/attr [:login-bar :error-message] "role")))

              (is
               (= "login-bar--error"
                  (sdk/attr [:login-bar :password] "aria-describedby")))

              (is
               (focus-becomes? "login-bar--password")))))))

(deftest escape-returns-the-focus-to-the-opener
  (with-gallery
    (step "Escape gives the focus back to the button that opened the dialog"
          (fn []
            (open-login!)
            (sdk/press! "Escape")
            (dialog/wait-closed! dialog-el)

            (is
             (focus-becomes? "login-bar--login"))))

    (step "an opener that is gone hands the focus to the fallback element"
          (fn []
            (let [opener
                  (sdk/el [:login :states :login])]
              (.click opener)
              (dialog/wait-open! dialog-el)
              (.evaluate opener "element => element.style.display = 'none'")
              (sdk/press! "Escape")
              (dialog/wait-closed! dialog-el)

              (is
               (focus-becomes? "login-bar--register"))

              (.evaluate opener "element => element.style.display = ''"))))))

(defn- ask-reset!
  [email]
  (open-login!)
  (button/click! [:login-bar :forgot-password])
  (text-field/fill! [:login-bar :username] email)
  (button/click! [:login-bar :submit-reset-password]))

(defn- reset-step-text
  []
  (sdk/wait-visible (sdk/el [:login-bar :notice]))
  (dialog-text))

(deftest reset-with-a-known-and-an-unknown-address-looks-the-same
  (with-gallery
    (let [seen
          (answer-cognito! (fn [target {:keys [Username]}]
                             (cond
                               (not= "ForgotPassword" target)
                               (refusal "InvalidParameterException" "User is already confirmed.")

                               (= "known@example.com" Username)
                               [200 {:CodeDeliveryDetails {:DeliveryMedium "EMAIL" :Destination "k***@e***"}}]

                               :else
                               (refusal "UserNotFoundException" "Username/client id combination not found."))))

          known
          (atom nil)]
      (step "a known address goes to the code step with a neutral notice"
            (fn []
              (ask-reset! "known@example.com")
              (reset! known (reset-step-text))

              (is
               (= "If this address has an account, a code is on its way. It can take a minute."
                  (sdk/text [:login-bar :notice])))

              (is
               (= "status"
                  (sdk/attr [:login-bar :notice] "role")))))

      (step "the code field comes above the new password"
            (fn []
              (is
               (< (.y (.boundingBox (sdk/el [:login-bar :confirmation-code])))
                  (.y (.boundingBox (sdk/el [:login-bar :password])))))))

      (step "Resend code asks for a reset code again and says a new one is on its way"
            (fn []
              (button/click! [:login-bar :resend-code])

              (is
               (sdk/eventually= "A new code is on its way."
                                #(sdk/text [:login-bar :notice])))

              (is
               (= ["ForgotPassword" "ForgotPassword"]
                  (targets seen)))))

      (step "an unknown address shows exactly the same"
            (fn []
              (button/click! [:login-bar :close])
              (dialog/wait-closed! dialog-el)
              (ask-reset! "unknown@example.com")

              (is
               (= @known
                  (reset-step-text))))))))

(deftest reset-waits-for-the-answer-and-stays-when-offline
  (with-gallery
    (let [mode
          (atom :hold)

          seen
          (answer-cognito! (fn [_ _] @mode))]
      (step "while the code is asked for, the address step stays, busy"
            (fn []
              (ask-reset! "someone@example.com")

              (is
               (sdk/eventually= "Sending the code…"
                                #(sdk/attr [:login-bar :submit-reset-password] "aria-label")))

              (is
               (false?
                (button/enabled? [:login-bar :submit-reset-password])))

              (is
               (sdk/present? [:login-bar :username]))

              (.fulfill ^Route (first (:held @seen))
                        (answer 200 {:CodeDeliveryDetails {:DeliveryMedium "EMAIL"}}))

              (is
               (sdk/becomes-visible? [:login-bar :confirmation-code]))))

      (step "a request that does not reach Cognito keeps the address step and says so"
            (fn []
              (reset! mode :abort)
              (button/click! [:login-bar :close])
              (dialog/wait-closed! dialog-el)
              (ask-reset! "someone@example.com")

              (is
               (sdk/eventually= "We couldn't reach the login service. Check your connection and try again."
                                #(when (sdk/present? [:login-bar :error-message]) (error-text))))

              (is
               (sdk/present? [:login-bar :username]))

              (is
               (button/enabled? [:login-bar :submit-reset-password])))))))

(deftest cognito-sentences-are-never-shown
  (with-gallery
    (answer-cognito! (fn [_ _]
                       (refusal "LimitExceededException" "Exceeded daily email limit for the operation or the account.")))
    (step "the daily email limit is said in plain words"
          (fn []
            (ask-reset! "someone@example.com")

            (is
             (sdk/eventually= "No more emails can be sent today. Try again tomorrow."
                              #(when (sdk/present? [:login-bar :error-message]) (error-text))))))))

(deftest the-dialog-fills-a-phone
  (with-gallery
    (.setViewportSize (sdk/page) 390 844)
    (step "below 600 px the dialog takes the whole screen"
          (fn []
            (open-login!)

            (is
             (sdk/eventually= 390.0
                              #(.width (.boundingBox (sdk/within dialog-el ".MuiDialog-paper")))))))))
