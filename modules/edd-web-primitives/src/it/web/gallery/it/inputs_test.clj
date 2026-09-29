(ns web.gallery.it.inputs-test
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.gallery.pages.button-elements :as button-el]
   [web.primitives.sdk.autocomplete :as autocomplete]
   [web.primitives.sdk.button :as button]
   [web.primitives.sdk.checkbox :as checkbox]
   [web.primitives.sdk.display :as display]
   [web.primitives.sdk.radio :as radio]
   [web.primitives.sdk.select :as select]
   [web.primitives.sdk.slider :as slider]
   [web.primitives.sdk.switch :as switch]
   [web.primitives.sdk.text-field :as text-field]))

(deftest button-counts-clicks
  (with-gallery
    (step "open button page" #(h/open-page! :button))

    (step "each click increments the counter"
          (fn []
            (let [btn     (button-el/variant :contained)
                  counter (button-el/variant-readout :contained)]

              (is
               (= "clicks:0"
                  (display/text counter)))

              (button/click! btn)
              (button/click! btn)

              (is
               (= "clicks:2"
                  (display/text=? counter "clicks:2"))))))))

(deftest button-shows-label
  (with-gallery
    (step "open button page" #(h/open-page! :button))

    (step "label is the button text"
          (fn []
            (is
             (= "Contained"
                (button/label (button-el/variant :contained))))))))

(deftest button-disabled-is-disabled
  (with-gallery
    (step "open button page" #(h/open-page! :button))

    (step "disabled button cannot be clicked"
          (fn []
            (is
             (false?
              (button/enabled? button-el/disabled)))))))

(deftest checkbox-toggles
  (with-gallery
    (step "open checkbox page" #(h/open-page! :checkbox))

    (step "check and uncheck"
          (fn []
            (let [box (h/el :checkbox :basic)]

              (is
               (false?
                (checkbox/checked? box)))

              (checkbox/check! box)

              (is
               (true?
                (checkbox/checked? box)))

              (is
               (= "checked:true"
                  (display/text=? (h/readout :checkbox :basic) "checked:true")))

              (checkbox/uncheck! box)

              (is
               (false?
                (checkbox/checked? box))))))))

(deftest checkbox-initially-checked
  (with-gallery
    (step "open checkbox page" #(h/open-page! :checkbox))

    (step "prechecked variant starts checked"
          (fn []
            (is
             (true?
              (checkbox/checked? (h/el :checkbox :checked))))))))

(deftest checkbox-disabled-is-disabled
  (with-gallery
    (step "open checkbox page" #(h/open-page! :checkbox))

    (step "disabled checkbox is not enabled"
          (fn []
            (is
             (false?
              (checkbox/enabled? (h/el :checkbox :disabled))))))))

(deftest switch-toggles
  (with-gallery
    (step "open switch page" #(h/open-page! :switch))

    (step "turn on and off"
          (fn []
            (let [sw (h/el :switch :labelled)]

              (is
               (false?
                (switch/on? sw)))

              (switch/turn-on! sw)

              (is
               (= "on:true"
                  (display/text=? (h/readout :switch :labelled) "on:true")))

              (switch/turn-off! sw)

              (is
               (= "on:false"
                  (display/text=? (h/readout :switch :labelled) "on:false"))))))))

(deftest switch-two-values
  (with-gallery
    (step "open switch page" #(h/open-page! :switch))

    (step "toggling picks the other value"
          (fn []
            (let [sw (h/el :switch :two-value)]

              (is
               (= "selected:one"
                  (display/text (h/readout :switch :two-value))))

              (switch/toggle! sw)

              (is
               (= "selected:two"
                  (display/text=? (h/readout :switch :two-value) "selected:two"))))))))

(deftest text-field-accepts-input
  (with-gallery
    (step "open text field page" #(h/open-page! :text-field))

    (step "fill updates the value"
          (fn []
            (let [field (h/el :text-field :basic)]

              (text-field/fill! field "hello world")

              (is
               (= "hello world"
                  (text-field/value field)))

              (is
               (= "value:hello world"
                  (display/text=? (h/readout :text-field :basic) "value:hello world"))))))))

(deftest text-field-prefilled-and-label
  (with-gallery
    (step "open text field page" #(h/open-page! :text-field))

    (step "prefilled value and label are shown"
          (fn []
            (let [field (h/el :text-field :prefilled)]

              (is
               (= "hello"
                  (text-field/value field)))

              (is
               (= "Field"
                  (text-field/label field))))))))

(deftest text-field-read-only-and-disabled
  (with-gallery
    (step "open text field page" #(h/open-page! :text-field))

    (step "read only and disabled variants"
          (fn []
            (is
             (true?
              (text-field/read-only? (h/el :text-field :read-only))))

            (is
             (false?
              (text-field/enabled? (h/el :text-field :disabled))))))))

(deftest number-field-reports-cents
  (with-gallery
    (step "open number field page" #(h/open-page! :number-field))

    (step "typing 12 reports 1200 cents"
          (fn []
            (let [field (h/el :number-field :number)]

              (text-field/clear! field)
              (text-field/type! field "12")

              (is
               (= "cents:1200"
                  (display/text=? (h/readout :number-field :number) "cents:1200"))))))))

(deftest number-field-expands-k-suffix
  (with-gallery
    (step "open number field page" #(h/open-page! :number-field))

    (step "2k expands to 2000"
          (fn []
            (let [field (h/el :number-field :number)]

              (text-field/clear! field)
              (text-field/type! field "2k")

              (is
               (= "cents:200000"
                  (display/text=? (h/readout :number-field :number) "cents:200000"))))))))

(deftest select-changes-value
  (with-gallery
    (step "open select page" #(h/open-page! :select))

    (step "choosing an option updates the value"
          (fn []
            (let [sel (h/el :select :simple)]

              (is
               (= "One"
                  (select/selected sel)))

              (select/choose! sel "Two")

              (is
               (= "Two"
                  (select/selected sel)))

              (is
               (= "value:Two"
                  (display/text=? (h/readout :select :simple) "value:Two"))))))))

(deftest select-lists-options
  (with-gallery
    (step "open select page" #(h/open-page! :select))

    (step "options are listed in order"
          (fn []
            (is
             (= ["One" "Two" "Three"]
                (select/options (h/el :select :simple))))))))

(deftest form-select-changes-value
  (with-gallery
    (step "open select page" #(h/open-page! :select))

    (step "form select picks B"
          (fn []
            (let [sel (h/el :select :form)]

              (select/choose! sel "B")

              (is
               (= "value:B"
                  (display/text=? (h/readout :select :form) "value:B"))))))))

(deftest autocomplete-single-select
  (with-gallery
    (step "open autocomplete page" #(h/open-page! :autocomplete))

    (step "search filters and choose selects"
          (fn []
            (let [ac (h/el :autocomplete :basic)]

              (autocomplete/search! ac "Be")

              (is
               (= ["Beta"]
                  (autocomplete/visible-options ac)))

              (autocomplete/choose! ac "Beta")

              (is
               (= "Beta"
                  (autocomplete/value ac)))

              (is
               (= "value:Beta"
                  (display/text=? (h/readout :autocomplete :basic) "value:Beta"))))))))

(deftest autocomplete-multiple-select
  (with-gallery
    (step "open autocomplete page" #(h/open-page! :autocomplete))

    (step "choosing two values adds two chips"
          (fn []
            (let [ac (h/el :autocomplete :multiple)]

              (autocomplete/choose! ac "Red")
              (autocomplete/choose! ac "Blue")

              (is
               (= ["Red" "Blue"]
                  (autocomplete/chips ac)))

              (is
               (= "count:2"
                  (display/text=? (h/readout :autocomplete :multiple) "count:2"))))))))

(deftest radio-selects-value
  (with-gallery
    (step "open radio page" #(h/open-page! :radio))

    (step "choosing b updates the value"
          (fn []
            (let [group (h/el :radio :vertical)]

              (is
               (= ["a" "b" "c"]
                  (radio/values group)))

              (is
               (= "a"
                  (radio/selected group)))

              (radio/choose! group "b")

              (is
               (= "b"
                  (radio/selected group)))

              (is
               (= "value:b"
                  (display/text=? (h/readout :radio :vertical) "value:b"))))))))

(deftest slider-moves
  (with-gallery
    (step "open slider page" #(h/open-page! :slider))

    (step "set-value! moves the thumb"
          (fn []
            (let [s (h/el :slider :basic)]

              (is
               (= 30.0
                  (slider/value s)))

              (slider/set-value! s 35)

              (is
               (= 35.0
                  (slider/value s)))

              (is
               (= "value:35"
                  (display/text=? (h/readout :slider :basic) "value:35"))))))))

(deftest slider-steps-and-disabled
  (with-gallery
    (step "open slider page" #(h/open-page! :slider))

    (step "stepped slider jumps by 10; disabled slider is disabled"
          (fn []
            (let [s (h/el :slider :steps)]

              (slider/set-value! s 70)

              (is
               (= 70.0
                  (slider/value s)))

              (is
               (false?
                (slider/enabled? (h/el :slider :disabled)))))))))
