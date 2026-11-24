(ns edd.client-test
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [edd.client :as client]))

(deftest a-timeout-becomes-an-abort-signal
  (testing "a request with :timeout gets a signal for it and no :timeout"
    (let [options
          (client/fetch-options {:method :post :timeout 31000} (fn [ms] [:signal ms]))]
      (is
       (= [:signal 31000]
          (:signal options)))

      (is
       (not (contains? options :timeout)))

      (is
       (= :post
          (:method options)))))

  (testing "a request without :timeout is sent as it is"
    (is
     (= {:method :put}
        (client/fetch-options {:method :put} (fn [_] :never))))))

(deftest post-deadline-defaults-to-121-seconds
  (with-redefs [client/get-config (constantly {})]
    (is
     (= 121000
        (:signal (client/fetch-options (client/post-params {}) identity)))))

  (with-redefs [client/get-config (constantly {:post-params {:timeout 45000}})]
    (is
     (= 45000
        (:timeout (client/post-params {}))))))

(deftest the-timeout-signal-aborts-with-a-timeout-error
  (async done
         (let [signal
               (client/timeout-signal 0)]
           (is
            (false? (.-aborted signal)))

           (.addEventListener signal "abort"
                              (fn []
                                (is
                                 (= "TimeoutError"
                                    (.. signal -reason -name)))

                                (done))))))
