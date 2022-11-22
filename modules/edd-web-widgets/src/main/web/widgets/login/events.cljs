(ns web.widgets.login.events
  (:require
   [re-frame.core :as rf]
   [web.widgets.login.db :as db]
   [clojure.string :as str]
   [edd.db :as edd-db]
   [edd.events :as edd-events]))

(rf/reg-event-fx
 ::initialize-db
 (fn [{:keys [db]} _]
   {:db (merge db/default-db
               (assoc-in db [::db/username] ""))
    :ensure-credentials {:on-success [::set-init? true]
                         :on-failure [::set-init? true]}}))

(rf/reg-event-db
 ::set-init?
 (fn [db [_ init?]]
   (assoc-in db [::db/init?] init?)))

(rf/reg-event-fx
 ::init
 (fn [{:keys [db]} [_ {:keys [language-switcher on-logout-hook]
                       :or   {language-switcher false}}]]
   {:db (-> db
            (assoc-in [::db/show-language-switcher?] language-switcher)
            (assoc-in [::db/on-logout-hook] on-logout-hook))}))

(rf/reg-event-db
 ::username-change
 (fn [db [_ value]]
   (assoc db ::db/username (-> value
                               (str/lower-case)
                               (str/trim)))))

(rf/reg-event-db
 ::password-change
 (fn [db [_ value]]
   (assoc db ::db/password value)))

(rf/reg-event-db
 ::set-event-after-login
 (fn [db [_ do-after-login]]
   (assoc-in db [::db/do-after-login] do-after-login)))

(rf/reg-event-fx
 ::login-succeeded
 (fn [{:keys [db]} [_ auth]]
   (let [do-after-login (get-in db [::db/do-after-login])]
     {:db (assoc-in db [::edd-db/user] auth)
      :fx [[:dispatch [::close-dialog]]
           [:dispatch [::edd-events/load-application do-after-login]]]})))

(defn request-code
  [db]
  [:amplify-resend-confirmation-code {:username (::db/username db)}])

(rf/reg-event-fx
 ::login-failed
 (fn [{:keys [db]} [_ {:keys [message type]}]]
   (if (= "UserNotConfirmedException" type)
     {:db (assoc db ::db/form-type :confirm-login)
      :fx [(request-code db)]}
     {:db (assoc db ::db/error-message-visible true
                 ::db/error-message message)})))

(rf/reg-event-fx
 ::do-login
 (fn [{:keys [db]}]
   {:db db
    :fx [[:amplify-login {:username   (::db/username db)
                          :password   (::db/password db)
                          :on-success [::login-succeeded]
                          :on-failure [::login-failed]}]]}))

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
               ::db/error-message message)}))

(rf/reg-event-fx
 ::submit-verification
 (fn [{:keys [db]} _]
   {:db db
    :fx [[:amplify-verify {:username   (::db/username db)
                           :code       (::db/confirmation-code db)
                           :on-success [::do-login]
                           :on-failure [::verification-failed]}]]}))

(rf/reg-event-fx
 ::login-and-proceed
 (fn [{:keys [db]} [_ on-success]]
   {:db       (assoc-in db [::db/do-after-login] on-success)
    :dispatch [::open-dialog :login]}))

(rf/reg-event-db
 ::open-dialog
 (fn [db [_ form-type]]
   (-> db
       (assoc ::db/form-type form-type)
       (assoc ::db/dialog-visible true))))

(rf/reg-event-db
 ::close-dialog
 (fn [db _]
   (assoc db ::db/dialog-visible false
          ::db/username ""
          ::db/password ""
          ::db/error-message-visible false
          ::db/error-message ""
          ::db/confirmation-visible false
          ::db/confirmation-code "")))

(rf/reg-event-db
 ::register-success
 (fn [db [_ {:keys [UserConfirmed]}]]
   (if (not UserConfirmed)
     (assoc db
            ::db/error-message-visible false
            ::db/error-message ""
            ::db/form-type :confirm-login)
     db)))

(rf/reg-event-db
 ::register-failed
 (fn [db [_ {:keys [message]}]]
   (assoc db ::db/error-message-visible true
          ::db/error-message message)))

(rf/reg-event-db
 ::confirmation-code-change
 (fn [db [_ value]]
   (assoc db ::db/confirmation-code value)))

(rf/reg-event-fx
 ::do-register
 (fn [{:keys [db]}]
   {:db db
    :fx [[:amplify-register {:username   (::db/username db)
                             :password   (::db/password db)
                             :on-success [::register-success]
                             :on-failure [::register-failed]}]]}))

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
          ::db/form-type :forgot-password)))

(rf/reg-event-fx
 ::reset-password
 (fn [{:keys [db]}]
   {:db (assoc db ::db/form-type :confirm-password-reset
               ::db/error-message ""
               ::db/password ""
               ::db/error-message-visible false)
    :fx [[:amplify-forgot-password {:username (::db/username db)}]]}))

(rf/reg-event-fx
 ::resend-code
 (fn [{:keys [db]}]
   {:db (assoc db
               ::db/error-message ""
               ::db/error-message-visible false)
    :fx [(request-code db)]}))

(rf/reg-event-fx
 ::confirm-reset-password
 (fn [{:keys [db]} _]
   {:db (assoc db ::db/form-type :confirm-password-reset)
    :fx [[:amplify-conform-forgot-password {:username   (::db/username db)
                                            :password   (::db/password db)
                                            :code       (::db/confirmation-code db)
                                            :on-success [::do-login]
                                            :on-failure [::register-failed]}]]}))

(rf/reg-event-db
 ::toggle-password-visibility
 (fn [db]
   (update-in db [::db/show-password?] not)))
