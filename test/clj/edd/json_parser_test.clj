(ns edd.json-parser-test
  (:require
   [clojure.test :refer :all]
   [edd.json :as json]))

(deftest test-json-field-setting
  (testing "Test json field setting"

    (testing ". should return a keyword"
      (is
       (= {:a  :b
           "a" :a}
          (json/parse-custom-fields {:a  :b
                                     "a" ":a"}))))

    (testing ". should return a string in case keyword starts with ::"
      (is
       (= {:a "::a"}
          (json/parse-custom-fields {:a "::a"}))))

    (testing ". should return a string in case string starts with : and contains space"
      (is
       (= {:a ": Some text"}
          (json/parse-custom-fields {:a ": Some text"}))))

    (testing ". should return a number"
      (is
       (= {:b  1}
          (json/parse-custom-fields {:b 1}))))

    (testing ". should return uuid"
      (is
       (= {:a #uuid "a05338b9-0a04-471f-aabf-51ffa4a5efd1"}
          (json/parse-custom-fields {:a "#a05338b9-0a04-471f-aabf-51ffa4a5efd1"}))))

    (testing ". should return a string in case uuid starts with ##"
      (is
       (= {:a "##a05338b9-0a04-471f-aabf-51ffa4a5efd1"}
          (json/parse-custom-fields {:a "##a05338b9-0a04-471f-aabf-51ffa4a5efd1"}))))

    (testing ". should return a key as uuid"

      (let [id (random-uuid)]
        (is (= {id :a}
               (json/parse-custom-fields {(keyword (str "#" id)) ":a"})))))))

(deftest test-json-field-encoding
  "Test json field encoding"
  (is (= {:a ":a"
          :b "::a"
          :c "#4fb62f2c-9c1d-4043-9c74-bbe2e017befc"
          :f [{:a ":b"}]
          :e 1
          :c1 "##4fb62f2c-9c1d-4043-9c74-bbe2e017befc"}
         (json/encode-custom-fields {:a :a
                                     :b ":a"
                                     :f [{:a :b}]
                                     :c #uuid "4fb62f2c-9c1d-4043-9c74-bbe2e017befc"
                                     :e 1
                                     :c1 "#4fb62f2c-9c1d-4043-9c74-bbe2e017befc"}))))

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

(deftest test-convert
  "Test converting edn keys"

  (are [expected input] (= expected (json/convert input))
    ":x" :x
    ":>aggregate/references" :>aggregate/references
    "#4fb62f2c-9c1d-4043-9c74-bbe2e017befc" #uuid "4fb62f2c-9c1d-4043-9c74-bbe2e017befc"
    "key" "key"
    [":a" 1] [:a 1]
    [{:a "#4fb62f2c-9c1d-4043-9c74-bbe2e017befc"}] [{:a #uuid "4fb62f2c-9c1d-4043-9c74-bbe2e017befc"}]
    1 1))

(run-tests 'edd.json-parser-test)

