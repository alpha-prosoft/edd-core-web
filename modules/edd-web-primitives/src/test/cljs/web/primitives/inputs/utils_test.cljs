(ns web.primitives.inputs.utils-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [cljs-time.core :as t]
            [web.primitives.inputs.utils :as utils]))

(deftest handle-comma-test
  (is (= "" (utils/handle-comma "")))
  (is (= "1" (utils/handle-comma "1")))
  (is (= "12" (utils/handle-comma "12")))
  (is (= "1." (utils/handle-comma "1.")))
  (is (= "1." (utils/handle-comma "1,")))
  (is (= "1.2" (utils/handle-comma "1,2")))
  (is (= "1.23" (utils/handle-comma "1,23")))
  (is (= "1.23" (utils/handle-comma "1,234")))
  (is (= "1.23" (utils/handle-comma "1,23abcde")))
  (is (= "1.23" (utils/handle-comma "qwertz1,23")))
  (is (= "291" (utils/handle-comma "(#291)&:-)"))))

(defn- capture-validate [props date]
  (let [calls (atom {:on-change [] :invalid []})

        props
        (merge
         {:component-min-date "1900-01-01"
          :component-max-date "2099-12-31"
          :set-date-input-invalid #(swap! calls update :invalid conj %)
          :on-change #(swap! calls update :on-change conj %)}
         props)]
    (utils/validate-date-min-max-date props date)
    @calls))

(deftest validate-in-range-date-propagates-and-is-valid
  (let [result
        (capture-validate {} (t/to-default-time-zone (t/date-time 2024 5 15)))]

    (is
     (= ["2024-05-15"]
        (:on-change result)))

    (is
     (= [false]
        (:invalid result)))))

(deftest validate-out-of-range-date-still-propagates-typed-value
  (let [result
        (capture-validate {} (t/to-default-time-zone (t/date-time 2 5 15)))]

    (is
     (= ["0002-05-15"]
        (:on-change result)))

    (is
     (= [true]
        (:invalid result)))))

(deftest date-out-of-range-checks-min-and-max
  (is
   (true?
    (utils/date-out-of-range?
     {:component-min-date "1900-01-01" :component-max-date "2099-12-31"}
     (t/date-time 1899 12 31))))

  (is
   (true?
    (utils/date-out-of-range?
     {:component-min-date "1900-01-01" :component-max-date "2099-12-31"}
     (t/date-time 2100 1 1))))

  (is
   (false?
    (utils/date-out-of-range?
     {:component-min-date "1900-01-01" :component-max-date "2099-12-31"}
     (t/date-time 2000 6 1)))))

(run-tests 'web.primitives.inputs.utils-test)
