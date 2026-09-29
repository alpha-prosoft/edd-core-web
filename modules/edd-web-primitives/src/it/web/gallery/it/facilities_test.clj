(ns web.gallery.it.facilities-test
  "The complex-form example: one reusable facility component rendered per
   facility. Tests address a field with the same :el-id vector the view uses,
   [:amount facility-id]."
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.button :as button]
   [web.primitives.sdk.checkbox :as checkbox]
   [web.primitives.sdk.core :as sdk]
   [web.primitives.sdk.display :as display]
   [web.primitives.sdk.select :as select]
   [web.primitives.sdk.text-field :as text-field]
   [web.primitives.sdk.values :as values]))

(def facility-a #uuid "0b6f1b8e-5c2a-4f7e-9a1d-2f4c8e6a1b01")

(def facility-b #uuid "7d3e9c41-8b2f-4a6d-b5e0-9c1f3a7d2e02")

(deftest each-facility-has-its-own-fields
  (with-gallery
    (step "open facilities section" #(h/open-page! :facilities))

    (step "the two facilities keep separate values"
          (fn []
            (is
             (= "12 months"
                (select/selected [:tenor facility-a])))

            (is
             (= "36 months"
                (select/selected [:tenor facility-b])))

            (is
             (true?
              (checkbox/checked? [:secured facility-a])))

            (is
             (false?
              (checkbox/checked? [:secured facility-b])))))))

(deftest editing-one-facility-leaves-the-other-alone
  (with-gallery
    (step "open facilities section" #(h/open-page! :facilities))

    (step "change facility B only"
          (fn []
            (select/choose! [:tenor facility-b] "24 months")
            (checkbox/check! [:secured facility-b])
            (text-field/clear! [:amount facility-b])
            (text-field/type! [:amount facility-b] "5")))

    (step "facility B changed, facility A did not"
          (fn []
            (is
             (= "amount:500 tenor:24 months secured:true"
                (display/text=? [:summary facility-b] "amount:500 tenor:24 months secured:true")))

            (is
             (= "amount:100000 tenor:12 months secured:true"
                (display/text [:summary facility-a])))))))

(defn- facility-ids-on-page []
  (let [cards
        (.locator (sdk/page) "[data-page-id='facilities'] [data-testid^='facility--']")]
    (set
     (for [i (range (.count cards))]
       (subs (.getAttribute (.nth cards i) "data-testid") (count "facility--"))))))

(deftest adding-a-facility-reuses-the-component
  (with-gallery
    (step "open facilities section" #(h/open-page! :facilities))

    (step "a new facility gets its own UUID and blank fields"
          (fn []
            (button/click! :add-facility)

            (let [new-id
                  (first (disj (facility-ids-on-page) (str facility-a) (str facility-b)))]

              (is
               (some? new-id))

              (is
               (= "12 months"
                  (select/selected [:tenor new-id])))

              (is
               (false?
                (checkbox/checked? [:secured new-id]))))))))

(deftest scan-nests-fields-per-facility
  (with-gallery
    (step "open facilities section" #(h/open-page! :facilities))

    (step "scan groups each field by facility id"
          (fn []
            (let [snapshot
                  (values/scan {:scope "[data-page-id='facilities']"})

                  a
                  (keyword (str facility-a))

                  b
                  (keyword (str facility-b))]

              (is
               (= #{a b}
                  (set (keys (:tenor snapshot)))))

              (is
               (= {:type :select :value "36 months"}
                  (get-in snapshot [:tenor b])))

              (is
               (= {:type :checkbox :value true :label "Secured"}
                  (get-in snapshot [:secured a]))))))))
