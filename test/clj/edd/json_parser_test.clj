(ns edd.json-parser-test
  (:require
   [clojure.test :refer :all]
   [edd.json :as json]))

(deftest test-json-field-setting
  "Test json field setting"
  (is (= {:a  :b
          "a" :a
          :b  1}
         (json/parse-custom-fields {:a  :b
                                    "a" ":a"
                                    :b  1}))))

(deftest test-json-field-encoding
  "Test json field encoding"
  (is (= {:a ":a"
          :b ":a"
          :c "#4fb62f2c-9c1d-4043-9c74-bbe2e017befc"
          :f [{:a ":b"}]
          :e 1}
         (json/encode-custom-fields {:a :a
                                     :b ":a"
                                     :f [{:a :b}]
                                     :c #uuid "4fb62f2c-9c1d-4043-9c74-bbe2e017befc"
                                     :e 1}))))

(deftest test-json-parser
  "Test json parser"
  (is (= {:a :a
          :b "b"
          :c 1}
         (json/custom-json-parser
          false true
          "{\"a\":\":a\",
               \"b\":\"b\",
               \"c\":1}"))))

(deftest test-json-field-setting
  "Test json field setting"
  (is (= {:a  :b
          "a" :a
          :b  1}
         (json/parse-custom-fields {:a  :b
                                    "a" ":a"
                                    :b  1}))))

(deftest test-json-field-setting
  "Test json field setting"
  (let [id (random-uuid)]
    (is (= {:a  :b
            id :a
            :b  1}
           (json/parse-custom-fields {:a  :b
                                      (keyword
                                       (str "#"
                                            id)) ":a"
                                      :b  1})))))
(run-tests 'edd.json-parser-test)

