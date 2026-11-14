(ns projectname.home-it
  (:require [clojure.test :refer [deftest is]]
            [projectname.about.elements :as about-el]
            [projectname.helpers :as h]
            [projectname.home.elements :as el]
            [testing.pw :refer [step]]
            [web.primitives.sdk.button :as button]
            [web.primitives.sdk.display :as display]))

(deftest increment-counts-clicks
  (h/with-app
    (step "open home"
          #(h/open! "/" el/title))

    (step "click increment twice"
          (fn []
            (button/click! el/increment)
            (button/click! el/increment)

            (is
             (= "Current count: 2"
                (display/text=? el/clicks "Current count: 2")))))))

(deftest item-opens-about-page
  (h/with-app
    (step "open home"
          #(h/open! "/" el/title))

    (step "open the first item"
          (fn []
            (button/click! (el/item 1))

            (is
             (= "About item: 1"
                (display/text=? about-el/title "About item: 1")))))))
