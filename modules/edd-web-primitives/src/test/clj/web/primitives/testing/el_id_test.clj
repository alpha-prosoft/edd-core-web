(ns web.primitives.testing.el-id-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [web.primitives.testing.el-id :refer [el-id]]))

(defn- first-name []
  (el-id :first-name))

(defn- person-field [person field]
  (el-id :person person field))

(deftest keyword-resolves-to-its-name
  (testing "plain keyword"
    (is
     (= "first-name"
        (el-id :first-name))))

  (testing "namespaced keyword keeps the namespace"
    (is
     (= "app.person/first-name"
        (el-id :app.person/first-name)))))

(deftest vector-joins-suffixes-with-separator
  (testing "keyword base with suffixes"
    (is
     (= "person--bla--ble"
        (el-id [:person "Bla" "ble"]))))

  (testing "suffixes are cleaned the same way as the variadic form"
    (is
     (= (el-id :person "Hello World" :Amount)
        (el-id [:person "Hello World" :Amount])))

    (is
     (= "person--hello-world--amount"
        (el-id [:person "Hello World" :Amount]))))

  (testing "a single-element vector is just the base"
    (is
     (= "person"
        (el-id [:person])))))

(deftest fn-vector-calls-the-element-function
  (testing "no-arg element fn"
    (is
     (= "first-name"
        (el-id [first-name]))))

  (testing "element fn with arguments"
    (is
     (= "person--bla--ble"
        (el-id [person-field "Bla" "ble"])))))

(deftest strings-and-nil
  (testing "string passes through"
    (is
     (= "already-built"
        (el-id "already-built"))))

  (testing "nil stays nil"
    (is
     (nil?
      (el-id nil)))))

(deftest variadic-form-is-unchanged
  (testing "two-arg form still builds base--suffix"
    (is
     (= "button--contained"
        (el-id :button "contained"))))

  (testing "keyword suffix uses its name"
    (is
     (= "button--contained"
        (el-id :button :contained)))))

(deftest ids-split-back-into-a-path
  (testing "vector el-ids round-trip through the separator"
    (is
     (= ["person" "bla" "ble"]
        (str/split (el-id [:person "Bla" "ble"]) #"--")))))
