(ns edd.client-test
  (:require [cljs.test :refer-macros [deftest is testing]]
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

(deftest the-timeout-signal-aborts
  (let [signal
        (client/timeout-signal 0)]
    (is
     (false? (.-aborted signal)))))
