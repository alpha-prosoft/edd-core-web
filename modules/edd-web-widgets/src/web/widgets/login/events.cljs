(ns web.widgets.login.events
  (:require
   [re-frame.core :as rf]
   [web.widgets.login.db :as db]
   [clojure.string :as str]
   [edd.db :as edd-db]
   [edd.events :as edd-events]
   [web.widgets.login.utils :as utils]))

(rf/reg-event-fx
 ::initialize-db
 (fn [{:keys [db]} _]
   {:db (merge db/default-db
               (-> db
                   (assoc-in [::db/username] nil)
                   (assoc-in [::db/init?] true)))}))

(rf/reg-event-db
 ::set-init?
 (fn [db [_ init?]]
   (assoc-in db [::db/init?] init?)))

(defn init
  "Options of the widget: `:remember-email` (default true) offers the email address that logged in last;
   `:register-note` is shown under the sign-up form (a reagent component, or `{:text \"…\" :links [{:label :href}]}`);
   `:translations` overrides texts per language (`{:en {:login \"Login\"}}`); `:focus-fallback` (a CSS selector or a
   fn returning an element) gets the focus when the dialog closes and what opened it is gone; `:announce` (a fn of a
   text) speaks the dialog's notices through the app's own live region instead of the dialog's; `:retry-delays`
   (default [1000 3000], [] for none) are the waits before a request that did not reach Cognito is sent again;
   `:region` is Cognito's region (default: the prefix of the user pool id)."
  [{:keys [db]} [_ {:keys [language-switcher on-logout-hook remember-email register-note translations focus-fallback
                           announce retry-delays region]
                    :or   {language-switcher false
                           remember-email    true
                           retry-delays      utils/default-retry-delays}}]]
  {:db (assoc db
              ::db/show-language-switcher? language-switcher
              ::db/on-logout-hook on-logout-hook
              ::db/remember-email? (boolean remember-email)
              ::db/register-note register-note
              ::db/translations translations
              ::db/focus-fallback focus-fallback
              ::db/announce announce
              ::db/retry-delays (vec retry-delays)
              ::db/region region)})

(rf/reg-event-fx
 ::init
 init)

(defn remember-email?
  [db]
  (get db ::db/remember-email? true))

(defn cognito-request
  "A request of the dialog to Cognito, with how it is sent: `:retry-delays` and `:region`."
  [db request]
  (assoc request
         :retry-delays (get db ::db/retry-delays utils/default-retry-delays)
         :region (::db/region db)))

(rf/reg-cofx
 ::remembered-email
 (fn [cofx _]
   (assoc cofx :remembered-email (try
                                   (.getItem js/localStorage db/remembered-email-key)
                                   (catch :default _ nil)))))

(rf/reg-fx
 ::remember-email
 (fn [email]
   (try
     (.setItem js/localStorage db/remembered-email-key email)
     (catch :default _ nil))))

(rf/reg-event-db
 ::username-change
 (fn [db [_ value]]
   (assoc db ::db/username value)))

(rf/reg-event-db
 ::password-change
 (fn [db [_ value]]
   (assoc db ::db/password value)))

(rf/reg-event-db
 ::set-event-after-login
 (fn [db [_ do-after-login]]
   (assoc-in db [::db/do-after-login] do-after-login)))

(defn close-dialog
  [db]
  (-> db
      (assoc ::db/dialog-visible false
             ::db/username ""
             ::db/password ""
             ::db/error-message-visible false
             ::db/error-message ""
             ::db/confirmation-visible false
             ::db/confirmation-code ""
             ::db/confirmed? false
             ::db/loading? false
             ::db/resending? false
             ::db/notice nil)))

(rf/reg-event-db
 ::close-dialog
 (fn [db _]
   (close-dialog db)))

(defn login-suceeded
  [db auth]
  (-> (assoc-in db [::edd-db/user] auth)
      close-dialog))

(defn login-succeeded
  [{:keys [db]} [_ auth]]
  (let [do-after-load
        (get-in db [::db/interrupted-event])

        config
        (::edd-db/config db)

        email
        (utils/sent-email (::db/username db))

        remember?
        (and (remember-email? db)
             (some? (utils/validate-email email)))]
    {:db (login-suceeded db auth)
     :fx [(when remember?
            [::remember-email email])
          (cond
            (or (get config :ApplicationId)
                (get config :ApplicationName))
            [:dispatch [::edd-events/load-application do-after-load]]
            do-after-load [:dispatch do-after-load])]}))

(rf/reg-event-fx
 ::login-succeeded
 login-succeeded)

(defn request-code
  [db]
  [:amplify-resend-confirmation-code (cognito-request db {:username   (::db/username db)
                                                          :on-success [::resend-code-succeeded]
                                                          :on-failure [::register-failed]})])

(rf/reg-event-fx
 ::confirm-account
 (fn [{:keys [db]} _]
   {:db (assoc db
               ::db/form-type :confirm-login
               ::db/error-message ""
               ::db/error-message-visible false)
    :fx [(request-code db)]}))

(rf/reg-event-db
 ::resend-code-succeeded
 (fn [db _]
   (assoc db ::db/error-message-visible false
          ::db/error-message "")))

(rf/reg-event-fx
 ::login-failed
 (fn [{:keys [db]} [_ {:keys [message type]}]]
   (if (= "UserNotConfirmedException" type)
     {:db (assoc db ::db/form-type :confirm-login
                 ::db/loading? false)
      :fx [(request-code db)]}
     {:db (assoc db ::db/error-message-visible true
                 ::db/error-message message
                 ::db/loading? false)})))

(rf/reg-event-fx
 ::do-login
 (fn [{:keys [db]}]
   {:db (assoc db ::db/loading? true
               ::db/error-message-visible false)
    :fx [[:amplify-login (cognito-request db {:username   (::db/username db)
                                              :password   (::db/password db)
                                              :on-success [::login-succeeded]
                                              :on-failure [::login-failed]})]]}))

(rf/reg-event-fx
 ::logout
 (fn [{:keys [db]}]
   (let [on-logout-hook (get-in db [::db/on-logout-hook])]
     (when (some? on-logout-hook)
       (on-logout-hook))
     {:db (assoc-in db [::edd-db/user] nil)
      :fx [[:amplify-logout]]})))

(rf/reg-event-fx
 ::verification-failed
 (fn [{:keys [db]} [_ {:keys [message]}]]
   {:db (assoc db ::db/error-message-visible true
               ::db/error-message message
               ::db/loading? false)}))

(rf/reg-event-fx
 ::verification-succeeded
 (fn [{:keys [db]} _]
   {:db (assoc db ::db/confirmed? true)
    :dispatch [::do-login]}))

(rf/reg-event-fx
 ::submit-verification
 (fn [{:keys [db]} _]
   {:db (assoc db ::db/loading? true
               ::db/error-message-visible false)
    :fx [(if (::db/confirmed? db)
           [:dispatch [::do-login]]
           [:amplify-verify (cognito-request db {:username   (::db/username db)
                                                 :code       (::db/confirmation-code db)
                                                 :on-success [::verification-succeeded]
                                                 :on-failure [::verification-failed]})])]}))

(rf/reg-event-fx
 ::login-and-proceed
 (fn [{:keys [db]} [_ on-success]]
   {:db       (assoc-in db [::db/do-after-login] on-success)
    :dispatch [::open-dialog :login]}))

(defn open-dialog
  "Opens the dialog at `form-type`. The email address that logged in last is filled in when none is typed; it is read
   now, as the dialog opens, so it never overwrites what is typed."
  [{:keys [db remembered-email]} [_ form-type]]
  (let [opened
        (assoc db
               ::db/form-type form-type
               ::db/dialog-visible true
               ::db/error-message-visible false
               ::db/error-message ""
               ::db/notice nil)

        prefill?
        (and (remember-email? db)
             (str/blank? (::db/username db))
             (not (str/blank? remembered-email)))]
    {:db (if prefill?
           (assoc opened ::db/username remembered-email)
           opened)}))

(rf/reg-event-fx
 ::open-dialog
 [(rf/inject-cofx ::remembered-email)]
 open-dialog)

(rf/reg-event-db
 ::register-success
 (fn [db [_ {:keys [UserConfirmed]}]]
   (if (not UserConfirmed)
     (assoc db
            ::db/error-message-visible false
            ::db/error-message ""
            ::db/loading? false
            ::db/form-type :confirm-login)
     (assoc db ::db/loading? false))))

(rf/reg-event-db
 ::register-failed
 (fn [db [_ {:keys [message]}]]
   (assoc db ::db/error-message-visible true
          ::db/error-message message
          ::db/loading? false)))

(rf/reg-event-db
 ::confirmation-code-change
 (fn [db [_ value]]
   (assoc db ::db/confirmation-code value)))

(rf/reg-event-fx
 ::do-register
 (fn [{:keys [db]}]
   {:db (assoc db ::db/loading? true
               ::db/error-message-visible false)
    :fx [[:amplify-register (cognito-request db {:username   (::db/username db)
                                                 :password   (::db/password db)
                                                 :on-success [::register-success]
                                                 :on-failure [::register-failed]})]]}))

(rf/reg-event-db
 ::close-error-message
 (fn [db]
   (assoc db
          ::db/error-message-visible false)))

(rf/reg-event-db
 ::forgot-password
 (fn [db]
   (assoc db
          ::db/error-message-visible false
          ::db/error-message ""
          ::db/notice nil
          ::db/form-type :forgot-password)))

(defn reset-asked
  "Asks for a reset code. The dialog stays at the address, busy, until Cognito answered."
  [{:keys [db]} _]
  {:db (assoc db
              ::db/error-message ""
              ::db/error-message-visible false
              ::db/notice nil
              ::db/loading? true)
   :fx [[:amplify-forgot-password (cognito-request db {:username   (::db/username db)
                                                       :on-success [::reset-code-sent]
                                                       :on-failure [::reset-code-refused]})]]})

(rf/reg-event-fx
 ::reset-password
 reset-asked)

(defn reset-code-sent
  "The code step, with the same notice whether or not the address has an account."
  [db _]
  (assoc db
         ::db/form-type :confirm-password-reset
         ::db/password ""
         ::db/confirmation-code ""
         ::db/loading? false
         ::db/error-message ""
         ::db/error-message-visible false
         ::db/notice :reset-code-sent))

(rf/reg-event-db
 ::reset-code-sent
 reset-code-sent)

(defn reset-code-refused
  "An address without an account is answered exactly like one with an account, so nobody learns which addresses have
   one. Other refusals keep the dialog at the address and say why."
  [db [_ {:keys [type message]}]]
  (if (= "UserNotFoundException" type)
    (reset-code-sent db nil)
    (assoc db
           ::db/error-message-visible true
           ::db/error-message message
           ::db/loading? false)))

(rf/reg-event-db
 ::reset-code-refused
 reset-code-refused)

(rf/reg-event-db
 ::reset-password-succeeded
 (fn [db [_ _]]
   (assoc db ::db/loading? false)))

(defn resend-code
  "A new reset code (`ForgotPassword`) while a password is reset, else the code that confirms a new account
   (`ResendConfirmationCode`, which answers an existing account with \"User is already confirmed.\")."
  [{:keys [db]} _]
  (let [request
        (cognito-request db {:username   (::db/username db)
                             :on-success [::code-resent]
                             :on-failure [::code-resend-failed]})]
    {:db (assoc db
                ::db/error-message ""
                ::db/error-message-visible false
                ::db/notice nil
                ::db/resending? true)
     :fx [(if (= :confirm-password-reset (::db/form-type db))
            [:amplify-forgot-password request]
            [:amplify-resend-confirmation-code request])]}))

(rf/reg-event-fx
 ::resend-code
 resend-code)

(defn code-resent
  [db _]
  (assoc db
         ::db/resending? false
         ::db/notice :code-resent))

(rf/reg-event-db
 ::code-resent
 code-resent)

(defn code-resend-failed
  [db [_ {:keys [type message] :as refusal}]]
  (if (= "UserNotFoundException" type)
    (code-resent db refusal)
    (assoc db
           ::db/resending? false
           ::db/error-message-visible true
           ::db/error-message message)))

(rf/reg-event-db
 ::code-resend-failed
 code-resend-failed)

(rf/reg-event-fx
 ::confirm-reset-password
 (fn [{:keys [db]} _]
   {:db (assoc db ::db/form-type :confirm-password-reset
               ::db/loading? true
               ::db/error-message-visible false)
    :fx [[:amplify-conform-forgot-password (cognito-request db {:username   (::db/username db)
                                                                :password   (::db/password db)
                                                                :code       (::db/confirmation-code db)
                                                                :on-success [::do-login]
                                                                :on-failure [::register-failed]})]]}))

(rf/reg-event-db
 ::toggle-password-visibility
 (fn [db]
   (update-in db [::db/show-password?] not)))

(rf/reg-event-fx
 ::login-on-key-enter
 (fn [{:keys [db]} [_ event]]
   (let [key-enter? (= (.-key event) "Enter")
         form-type (get-in db [::db/form-type])
         username (get-in db [::db/username] "")
         username-invalid? (nil? (utils/validate-email username))
         password (str (get-in db [::db/password] ""))
         password-invalid? (some true? (vals (utils/validate-password password)))
         confirmation-code-empty? (str/blank? (get-in db [::db/confirmation-code]))]
     (cond
       (false? key-enter?) {}
       (and (= form-type :login)
            (not username-invalid?)
            (not password-invalid?))
       {:dispatch [::do-login]}
       (and (= form-type :register)
            (not username-invalid?)
            (not password-invalid?))
       {:dispatch [::do-register]}
       (and (= form-type :confirm-password-reset)
            (not password-invalid?)
            (not confirmation-code-empty?))
       {:dispatch [::confirm-reset-password]}
       (and (= form-type :forgot-password)
            (not username-invalid?))
       {:dispatch [::reset-password]}
       :else {}))))

(def submit-events
  {:login                  [::do-login]
   :register               [::do-register]
   :forgot-password        [::reset-password]
   :confirm-password-reset [::confirm-reset-password]
   :confirm-login          [::submit-verification]})

(defn submit-form
  "The dialog's form is sent with what its fields hold right now, typed or filled by the browser a moment ago: the
   values are taken, checked, and the step goes out. Nothing goes out while a request is under way."
  [{:keys [db]} [_ form-type {:keys [username password code]}]]
  (let [taken
        (cond-> (update db ::db/submits (fnil inc 0))
          (some? username) (assoc ::db/username username)
          (some? password) (assoc ::db/password password)
          (some? code)     (assoc ::db/confirmation-code (str/trim code)))

        problem
        (utils/submit-problem form-type {:username (::db/username taken)
                                         :password (::db/password taken)
                                         :code     (::db/confirmation-code taken)})]
    (cond
      (::db/loading? db)
      {}

      problem
      {:db (assoc taken
                  ::db/error-message problem
                  ::db/error-message-visible true)}

      :else
      {:db (assoc taken ::db/loading? true)
       :fx [[:dispatch (submit-events form-type)]]})))

(rf/reg-event-fx
 ::submit-form
 submit-form)
