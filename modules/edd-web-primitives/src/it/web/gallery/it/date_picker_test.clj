(ns web.gallery.it.date-picker-test
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.date-picker :as date-picker]
   [web.primitives.sdk.display :as display]))

(deftest day-first-typing-keeps-value
  (with-gallery
    (step "open date picker page" #(h/open-page! :date-picker))

    (step "typing day-first at human speed keeps the value"
          (fn []
            (let [dp (h/el :date-picker :basic)]

              (date-picker/type-date! dp "15052024" 400)

              (is
               (= "15.05.2024"
                  (date-picker/value dp)))

              (is
               (= "value:2024-05-15"
                  (display/text (h/readout :date-picker :basic)))))))))

(deftest out-of-range-year-keeps-day-and-month
  (with-gallery
    (step "open date picker page" #(h/open-page! :date-picker))

    (step "an out-of-range year keeps day and month"
          (fn []
            (let [dp (h/el :date-picker :basic)]

              (date-picker/type-date! dp "15050027" 400)

              (is
               (= "15.05.0027"
                  (date-picker/value dp))))))))

(deftest out-of-range-date-shows-error-after-blur
  (with-gallery
    (step "open date picker page" #(h/open-page! :date-picker))

    (step "a year before 1900 is flagged once the field loses focus"
          (fn []
            (let [dp (h/el :date-picker :basic)]

              (date-picker/set-date! dp "15.05.0027")
              (date-picker/blur! dp)

              (is
               (true?
                (date-picker/error? dp)))

              (is
               (= "Invalid date"
                  (date-picker/helper-text dp))))))))

(deftest prefilled-shows-value
  (with-gallery
    (step "open date picker page" #(h/open-page! :date-picker))

    (step "prefilled picker shows the formatted value"
          (fn []
            (is
             (= "15.09.2020"
                (date-picker/value (h/el :date-picker :prefilled))))))))

(deftest empty-picker-shows-placeholders
  (with-gallery
    (step "open date picker page" #(h/open-page! :date-picker))

    (step "an empty picker shows DD.MM.YYYY"
          (fn []
            (is
             (true?
              (date-picker/blank? (h/el :date-picker :basic))))))))

(deftest read-only-shows-value
  (with-gallery
    (step "open date picker page" #(h/open-page! :date-picker))

    (step "read only picker renders the date as text"
          (fn []
            (is
             (= "15.09.2020"
                (date-picker/value (h/el :date-picker :read-only))))))))
