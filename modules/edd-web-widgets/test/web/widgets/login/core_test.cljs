(ns web.widgets.login.core-test
  (:require [cljs.test :refer-macros [deftest is async]]
            [web.widgets.login.core :as core]))

(defn- reply
  [answer]
  (if (= :network answer)
    (js/Promise.reject (js/TypeError. "Failed to fetch"))
    (let [[status body]
          answer]
      (js/Promise.resolve (js/Response. (.stringify js/JSON (clj->js body)) #js {:status status})))))

(defn- send-with!
  "Sends a request to a Cognito that gives `answers` in turn and calls `check` with the URLs fetched, the outcome and
   the milliseconds it took."
  [answers delays done check]
  (let [original
        (.-fetch js/window)

        calls
        (atom [])

        started
        (js/Date.now)

        finish
        (fn [outcome]
          (set! (.-fetch js/window) original)
          (check @calls outcome (- (js/Date.now) started))
          (done))]
    (set! (.-fetch js/window)
          (fn [url _]
            (let [seen
                  (swap! calls conj url)]
              (reply (nth answers (dec (count seen)) (last answers))))))
    (core/send! {:url          "https://cognito-idp.eu-central-1.amazonaws.com"
                 :target       "InitiateAuth"
                 :body         {}
                 :retry-delays delays
                 :on-answer    #(finish [:answer (js->clj % :keywordize-keys true)])
                 :on-failure   #(finish [:failure %])})))

(deftest a-request-that-does-not-reach-cognito-is-sent-again
  (async done
         (send-with! [:network :network [200 {:ok true}]]
                     [30 60]
                     done
                     (fn [calls outcome elapsed]
                       (is
                        (= 3
                           (count calls)))

                       (is
                        (= [:answer {:ok true}]
                           outcome))

                       (is
                        (<= 90
                            elapsed))))))

(deftest after-the-last-retry-the-dialog-hears-of-it
  (async done
         (send-with! [:network]
                     [1 1]
                     done
                     (fn [calls outcome _]
                       (is
                        (= 3
                           (count calls)))

                       (is
                        (= [:failure core/network-error]
                           outcome))))))

(deftest a-refusal-of-cognito-is-not-sent-again
  (async done
         (send-with! [[400 {:__type "NotAuthorizedException" :message "Incorrect username or password."}]]
                     [1 1]
                     done
                     (fn [calls outcome _]
                       (is
                        (= 1
                           (count calls)))

                       (is
                        (= [:failure {:message :invalid-credentials :type "NotAuthorizedException"}]
                           outcome))))))

(deftest a-server-error-of-cognito-is-an-answer-too
  (async done
         (send-with! [[500 {:__type "InternalErrorException" :message "Internal"}]]
                     [1 1]
                     done
                     (fn [calls outcome _]
                       (is
                        (= 1
                           (count calls)))

                       (is
                        (= [:failure {:message :unknown-error :type "InternalErrorException"}]
                           outcome))))))

(deftest no-delays-no-retry
  (async done
         (send-with! [:network]
                     []
                     done
                     (fn [calls outcome _]
                       (is
                        (= 1
                           (count calls)))

                       (is
                        (= [:failure core/network-error]
                           outcome))))))

(deftest the-region-comes-from-the-pool-id-unless-given
  (is
   (= "eu-central-1"
      (core/cognito-region nil "eu-central-1_AbC123")))

  (is
   (= "us-east-1"
      (core/cognito-region "us-east-1" "eu-central-1_AbC123")))

  (is
   (= "eu-west-1"
      (core/cognito-region nil nil)))

  (is
   (= "https://cognito-idp.eu-central-1.amazonaws.com"
      (core/cognito-url "eu-central-1"))))
