(ns web.widgets.login.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.widgets.login.utils :as utils]))

(deftest cognito-refusals-become-plain-texts
  (testing "a wrong password and an unknown user read the same"
    (is
     (= :invalid-credentials
        (utils/cognito-error-key "NotAuthorizedException" "Incorrect username or password.")))

    (is
     (= :invalid-credentials
        (utils/cognito-error-key "UserNotFoundException" "User does not exist."))))

  (testing "codes"
    (is
     (= :invalid-code
        (utils/cognito-error-key "CodeMismatchException" "Invalid verification code provided, please try again.")))

    (is
     (= :code-expired
        (utils/cognito-error-key "ExpiredCodeException" "Invalid code provided, please request a code again."))))

  (testing "too many tries and the daily email limit"
    (is
     (= :attempt-limit-exceeded
        (utils/cognito-error-key "LimitExceededException" "Attempt limit exceeded, please try after some time.")))

    (is
     (= :attempt-limit-exceeded
        (utils/cognito-error-key "TooManyRequestsException" "Rate exceeded")))

    (is
     (= :attempt-limit-exceeded
        (utils/cognito-error-key "NotAuthorizedException" "Password attempts exceeded")))

    (is
     (= :email-limit-exceeded
        (utils/cognito-error-key "LimitExceededException" "Exceeded daily email limit for the operation or the account."))))

  (testing "password policy, existing account, unknown refusals"
    (is
     (= :invalid-password
        (utils/cognito-error-key "InvalidPasswordException" "Password did not conform with policy: Password must have uppercase characters")))

    (is
     (= :invalid-password
        (utils/cognito-error-key "InvalidParameterException" "1 validation error detected: Value at 'password' failed to satisfy constraint")))

    (is
     (= :user-exists
        (utils/cognito-error-key "UsernameExistsException" "An account with the given email already exists.")))

    (is
     (= :already-confirmed
        (utils/cognito-error-key "InvalidParameterException" "User is already confirmed.")))

    (is
     (= :unknown-error
        (utils/cognito-error-key "InternalErrorException" "Something only Cognito says")))))

(deftest the-email-is-trimmed-and-lower-cased-only-when-sent
  (is
   (= "someone@example.com"
      (utils/sent-email "  Someone@Example.COM ")))

  (is
   (nil?
    (utils/sent-email nil))))

(deftest submit-problem-judges-the-values-sent
  (is
   (nil?
    (utils/submit-problem :login {:username " Someone@Example.com " :password "x"})))

  (is
   (= :enter-email
      (utils/submit-problem :login {:username "someone" :password "x"})))

  (is
   (= :enter-password
      (utils/submit-problem :login {:username "someone@example.com" :password ""})))

  (is
   (= :enter-code
      (utils/submit-problem :confirm-password-reset {:password "Passw0rd-x" :code " "})))

  (is
   (= :invalid-password
      (utils/submit-problem :register {:username "someone@example.com" :password "short"})))

  (is
   (nil?
    (utils/submit-problem :forgot-password {:username "someone@example.com"}))))

(deftest errors-are-linked-to-their-field
  (is
   (= :password
      (utils/error-field :login :invalid-credentials)))

  (is
   (= :username
      (utils/error-field :login :enter-email)))

  (is
   (= :code
      (utils/error-field :confirm-password-reset :invalid-code)))

  (is
   (= :username
      (utils/error-field :forgot-password :network-error))))

(deftest the-focus-starts-in-the-first-empty-field
  (is
   (= :password
      (utils/start-field :login "someone@example.com")))

  (is
   (= :username
      (utils/start-field :login "")))

  (is
   (= :username
      (utils/start-field :register nil)))

  (is
   (= :username
      (utils/start-field :forgot-password "someone@example.com")))

  (is
   (= :code
      (utils/start-field :confirm-password-reset "someone@example.com"))))

(deftest the-region-is-the-prefix-of-the-pool-id
  (is
   (= "eu-central-1"
      (utils/pool-region "eu-central-1_AbC123")))

  (is
   (= "us-gov-west-1"
      (utils/pool-region "us-gov-west-1_x")))

  (is
   (nil?
    (utils/pool-region "no-region")))

  (is
   (nil?
    (utils/pool-region nil))))

(deftest a-request-is-sent-again-after-each-delay
  (is
   (= [1000 3000 nil]
      (mapv #(utils/retry-delay utils/default-retry-delays %) [0 1 2])))

  (is
   (nil?
    (utils/retry-delay [] 0))))
