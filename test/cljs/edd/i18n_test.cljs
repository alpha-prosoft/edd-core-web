(ns edd.i18n-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [edd.i18n :as i18n]
            [edd.db :as db]
            [re-frame.db :as re-frame-db]))

(defn setup-test-db
  [lang translations]
  (reset! re-frame-db/app-db
          {::db/selected-language lang
           ::db/translations translations}))

(deftest format-string-test
  (testing "format-string with positional parameters"
    (is (= "Hello Bob, welcome Alice!"
           (#'i18n/format-string "Hello {0}, welcome {1}!" ["Bob" "Alice"])))
    (is (= "User 1 has 5 items"
           (#'i18n/format-string "User {0} has {1} items" ["1" "5"]))))
  
  (testing "format-string with named parameters"
    (is (= "Hello Bob, welcome to Berlin!"
           (#'i18n/format-string "Hello {name}, welcome to {place}!" 
                                {:name "Bob" :place "Berlin"})))
    (is (= "User John has 10 points"
           (#'i18n/format-string "User {username} has {points} points" 
                                {:username "John" :points "10"}))))
  
  (testing "format-string with no parameters"
    (is (= "Hello World!" (#'i18n/format-string "Hello World!" nil)))
    (is (= "Hello World!" (#'i18n/format-string "Hello World!" [])))
    (is (= "Hello World!" (#'i18n/format-string "Hello World!" {})))))

(deftest tr-basic-test
  (testing "basic keyword translation"
    (setup-test-db :en {:en {:first-name "First Name"
                             :last-name "Last Name"}})
    (is (= "First Name" (i18n/tr :first-name)))
    (is (= "Last Name" (i18n/tr :last-name))))
  
  (testing "missing translation returns placeholder"
    (setup-test-db :en {:en {}})
    (is (= "{tr [:en :missing-key]}" (i18n/tr :missing-key)))))

(deftest tr-nested-test
  (testing "nested key translation"
    (setup-test-db :en {:en {:user {:first-name "First Name"
                                    :last-name "Last Name"}
                             :admin {:role "Administrator"}}})
    (is (= "First Name" (i18n/tr [:user :first-name])))
    (is (= "Last Name" (i18n/tr [:user :last-name])))
    (is (= "Administrator" (i18n/tr [:admin :role])))))

(deftest tr-map-syntax-test
  (testing "map syntax with simple message"
    (setup-test-db :en {:en {:greeting "Hello"}})
    (is (= "Hello" (i18n/tr {:message :greeting}))))
  
  (testing "map syntax with nested message"
    (setup-test-db :en {:en {:user {:welcome "Welcome User"}}})
    (is (= "Welcome User" (i18n/tr {:message [:user :welcome]})))))

(deftest tr-with-positional-params-test
  (testing "translation with positional parameters"
    (setup-test-db :en {:en {:greeting "Hello {0}, welcome {1}!"}})
    (is (= "Hello Bob, welcome Alice!"
           (i18n/tr {:message :greeting
                     :params ["Bob" "Alice"]}))))
  
  (testing "nested translation with positional parameters"
    (setup-test-db :en {:en {:user {:greeting "User {0} has {1} items"}}})
    (is (= "User John has 5 items"
           (i18n/tr {:message [:user :greeting]
                     :params ["John" "5"]})))))

(deftest tr-with-named-params-test
  (testing "translation with named parameters"
    (setup-test-db :en {:en {:greeting "Hello {name}, welcome to {place}!"}})
    (is (= "Hello Bob, welcome to Berlin!"
           (i18n/tr {:message :greeting
                     :params {:name "Bob" :place "Berlin"}}))))
  
  (testing "nested translation with named parameters"
    (setup-test-db :en {:en {:user {:profile "Name: {firstName} {lastName}, Age: {age}"}}})
    (is (= "Name: John Doe, Age: 30"
           (i18n/tr {:message [:user :profile]
                     :params {:firstName "John" 
                              :lastName "Doe" 
                              :age "30"}})))))

(deftest tr-language-switching-test
  (testing "switching between languages"
    (let [translations {:en {:greeting "Hello"}
                       :de {:greeting "Hallo"}
                       :es {:greeting "Hola"}}]
      (setup-test-db :en translations)
      (is (= "Hello" (i18n/tr :greeting)))
      
      (setup-test-db :de translations)
      (is (= "Hallo" (i18n/tr :greeting)))
      
      (setup-test-db :es translations)
      (is (= "Hola" (i18n/tr :greeting))))))

(deftest tr-error-handling-test
  (testing "throws error for non-string translation value"
    (setup-test-db :en {:en {:invalid-value {:nested "map"}}})
    (is (thrown? js/Error (i18n/tr :invalid-value)))))

(deftest tr-backwards-compatibility-test
  (testing "backwards compatibility with existing usage"
    (setup-test-db :en {:en {:simple "Simple"
                             :user {:name "User Name"}}})
    (is (= "Simple" (i18n/tr :simple)))
    (is (= "User Name" (i18n/tr [:user :name])))))