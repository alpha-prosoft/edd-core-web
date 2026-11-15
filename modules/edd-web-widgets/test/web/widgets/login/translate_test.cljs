(ns web.widgets.login.translate-test
  (:require [cljs.test :refer-macros [deftest is]]
            [web.widgets.login.i18n.translate :as translate]))

(deftest an-app-overrides-single-texts
  (let [merged
        (translate/deep-merge translate/lang-map {:en {:login  "Login"
                                                       :titles {:login "Login"}}})]
    (is
     (= ["Login" "Login" "Create an account" "Email address"]
        [(get-in merged [:en :login])
         (get-in merged [:en :titles :login])
         (get-in merged [:en :titles :register])
         (get-in merged [:en :username])]))))

(deftest every-language-has-every-text
  (let [key-paths
        (fn [lang]
          (set (for [[k v] (get translate/lang-map lang)
                     path (if (map? v)
                            (map #(vector k %) (keys v))
                            [[k]])]
                 path)))]
    (doseq [lang [:de :ru :ua]]
      (is
       (= (key-paths :en)
          (key-paths lang))
       (str lang)))))
