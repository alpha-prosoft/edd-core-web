(ns web.primitives.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.primitives.utils :as utils]))

(deftest el-id->string-test
  (testing "keyword el-id keeps namespace and drops leading colon"
    (is
     (= "samurai.tournament.elements/create-button"
        (utils/el-id->string :samurai.tournament.elements/create-button))))

  (testing "plain keyword without namespace"
    (is
     (= "create-button"
        (utils/el-id->string :create-button))))

  (testing "vector el-id joins suffixes"
    (is
     (= "person--bla--ble"
        (utils/el-id->string [:person "Bla" "ble"]))))

  (testing "string el-id passes through"
    (is
     (= "already-a-string"
        (utils/el-id->string "already-a-string"))))

  (testing "nil el-id yields nil"
    (is
     (nil?
      (utils/el-id->string nil)))))

(deftest with-el-id-as-id-test
  (testing "el-id becomes :id and :el-id is removed"
    (is
     (= {:id "ns/thing"}
        (utils/with-el-id-as-id {:el-id :ns/thing}))))

  (testing "vector el-id is resolved"
    (is
     (= {:id "person--first-name"}
        (utils/with-el-id-as-id {:el-id [:person :first-name]}))))

  (testing "existing explicit :id wins over :el-id"
    (is
     (= {:id "explicit"}
        (utils/with-el-id-as-id {:id "explicit" :el-id :ns/thing}))))

  (testing "props without :el-id are untouched"
    (is
     (= {:value "x"}
        (utils/with-el-id-as-id {:value "x"}))))

  (testing "non-map props pass through"
    (is
     (= "not-a-map"
        (utils/with-el-id-as-id "not-a-map")))))

(deftest with-el-id-as-testid-test
  (testing "el-id becomes :data-testid and :el-id is removed"
    (is
     (= {:data-testid "ns/card"}
        (utils/with-el-id-as-testid {:el-id :ns/card}))))

  (testing "existing explicit :data-testid wins over :el-id"
    (is
     (= {:data-testid "explicit"}
        (utils/with-el-id-as-testid {:data-testid "explicit" :el-id :ns/card}))))

  (testing "props without :el-id are untouched"
    (is
     (= {:color "primary"}
        (utils/with-el-id-as-testid {:color "primary"})))))
