(ns web.widgets.login.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.widgets.login.db :as db]
            [web.widgets.login.events :as events]))

(deftest the-form-is-sent-with-what-its-fields-hold
  (testing "values filled a moment ago (autofill) are taken and the step goes out"
    (let [result
          (events/submit-form {:db {::db/username "" ::db/password ""}}
                              [::events/submit-form :login {:username " Someone@Example.com "
                                                            :password "Passw0rd-x"
                                                            :code     nil}])]
      (is
       (= [[:dispatch [::events/do-login]]]
          (:fx result)))

      (is
       (= [" Someone@Example.com " "Passw0rd-x" true]
          [(get-in result [:db ::db/username])
           (get-in result [:db ::db/password])
           (get-in result [:db ::db/loading?])]))))

  (testing "a missing value is said instead of sending"
    (let [result
          (events/submit-form {:db {}} [::events/submit-form :login {:username "someone@example.com" :password ""}])]
      (is
       (nil?
        (:fx result)))

      (is
       (= [true :enter-password]
          [(get-in result [:db ::db/error-message-visible])
           (get-in result [:db ::db/error-message])]))))

  (testing "nothing goes out while a request is under way"
    (is
     (= {}
        (events/submit-form {:db {::db/loading? true}}
                            [::events/submit-form :login {:username "someone@example.com" :password "x"}])))))

(deftest a-reset-answers-every-address-the-same
  (let [asked
        (:db (events/reset-asked {:db {::db/form-type :forgot-password ::db/username "a@example.com"}} nil))

        known
        (events/reset-code-sent asked nil)

        unknown
        (events/reset-code-refused asked [::events/reset-code-refused {:type    "UserNotFoundException"
                                                                       :message :invalid-credentials}])]
    (testing "the address step stays busy until Cognito answered"
      (is
       (= [:forgot-password true]
          [(::db/form-type asked) (::db/loading? asked)])))

    (testing "a known and an unknown address lead to the same code step with the same notice"
      (is
       (= known
          unknown))

      (is
       (= [:confirm-password-reset :reset-code-sent false]
          [(::db/form-type known) (::db/notice known) (::db/error-message-visible known)])))

    (testing "a network failure keeps the address step"
      (let [offline
            (events/reset-code-refused asked [::events/reset-code-refused {:type "NetworkError" :message :network-error}])]
        (is
         (= [:forgot-password :network-error false]
            [(::db/form-type offline) (::db/error-message offline) (::db/loading? offline)]))))))

(deftest resend-asks-for-the-code-of-the-step
  (testing "the reset step asks for a new reset code"
    (is
     (= :amplify-forgot-password
        (ffirst (:fx (events/resend-code {:db {::db/form-type :confirm-password-reset}} nil))))))

  (testing "the sign-up confirmation asks for a new confirmation code"
    (is
     (= :amplify-resend-confirmation-code
        (ffirst (:fx (events/resend-code {:db {::db/form-type :confirm-login}} nil))))))

  (testing "the button is busy, then says a new code is on its way"
    (let [busy
          (:db (events/resend-code {:db {::db/form-type :confirm-password-reset}} nil))

          done
          (events/code-resent busy nil)]
      (is
       (true?
        (::db/resending? busy)))

      (is
       (= [false :code-resent]
          [(::db/resending? done) (::db/notice done)])))))

(deftest the-last-email-that-logged-in-is-offered
  (testing "it is read when the dialog opens and fills an empty field"
    (is
     (= "someone@example.com"
        (get-in (events/open-dialog {:db {} :remembered-email "someone@example.com"} [::events/open-dialog :login])
                [:db ::db/username]))))

  (testing "it never overwrites what is typed"
    (is
     (= "typed@example.com"
        (get-in (events/open-dialog {:db {::db/username "typed@example.com"} :remembered-email "someone@example.com"}
                                    [::events/open-dialog :login])
                [:db ::db/username]))))

  (testing "switched off, nothing is filled in"
    (is
     (= ""
        (get-in (events/open-dialog {:db {::db/username "" ::db/remember-email? false} :remembered-email "someone@example.com"}
                                    [::events/open-dialog :login])
                [:db ::db/username]))))

  (testing "a successful login remembers the address as it was sent"
    (is
     (= [::events/remember-email "someone@example.com"]
        (first (:fx (events/login-succeeded {:db {::db/username " Someone@Example.com "}} [::events/login-succeeded {}]))))))

  (testing "switched off, a login remembers nothing"
    (is
     (nil?
      (first (:fx (events/login-succeeded {:db {::db/username "someone@example.com" ::db/remember-email? false}}
                                          [::events/login-succeeded {}])))))))

(deftest options-have-defaults
  (let [db
        (:db (events/init {:db {}} [::events/init {}]))]
    (is
     (= [true nil nil]
        [(::db/remember-email? db) (::db/register-note db) (::db/translations db)]))))
