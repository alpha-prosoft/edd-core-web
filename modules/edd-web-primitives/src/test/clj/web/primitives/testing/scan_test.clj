(ns web.primitives.testing.scan-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [web.primitives.testing.el-id :refer [el-id]]
   [web.primitives.testing.scan :as scan]))

(def ^:private john
  {:type :input :value "John" :label "First name"})

(def ^:private h3
  {:type :text :text "h3 sample"})

(deftest path-splits-on-separator
  (is
   (= [:person :first-name]
      (scan/->path "person--first-name")))

  (is
   (= [:grid :row-1 :amount]
      (scan/->path "grid--row-1--amount"))))

(deftest reading-describes-controls
  (testing "input keeps value and label"
    (is
     (= {:type :input :value "hello" :label "Field"}
        (scan/reading {"type" "input" "value" "hello" "label" "Field"}))))

  (testing "missing label is left out, false value is kept"
    (is
     (= {:type :checkbox :value false}
        (scan/reading {"type" "checkbox" "value" false "label" nil})))))

(deftest reading-describes-text-elements
  (testing "non-controls carry their visible text, not a value"
    (is
     (= {:type :text :text "h3 sample"}
        (scan/reading {"type" "text" "text" "h3 sample"}))))

  (testing "empty text is still reported"
    (is
     (= {:type :text :text ""}
        (scan/reading {"type" "text" "text" ""})))))

(deftest nest-builds-nested-map
  (testing "flat and nested ids"
    (is
     (= {:first-name   john
         :some-element {:bla {:ble h3 :blo h3}}}
        (scan/nest [["first-name" john]
                    ["some-element--bla--ble" h3]
                    ["some-element--bla--blo" h3]])))))

(deftest nest-uses-el-id-built-ids
  (testing "ids built from el-id vectors nest back into their parts"
    (is
     (= {:person {:bla {:ble h3}}}
        (scan/nest [[(el-id [:person "Bla" "ble"]) h3]])))))

(deftest nest-skips-chrome-and-generated-ids
  (is
   (= {:first-name john}
      (scan/nest [["first-name" john]
                  ["gallery-section" h3]
                  ["variant-body-basic" h3]
                  [":r7:" h3]
                  ["CheckBoxIcon" h3]
                  ["" h3]]))))

(deftest nest-keeps-parent-under-self-key-on-collision
  (testing "a parent id is kept under :el/self next to its children, in either order"
    (is
     (= {:row {:el/self h3 :amount john}}
        (scan/nest [["row" h3]
                    ["row--amount" john]])))

    (is
     (= {:row {:el/self h3 :amount john}}
        (scan/nest [["row--amount" john]
                    ["row" h3]]))))

  (testing "an element and its readout both survive (gallery demo shape)"
    (is
     (= {:checkbox {:basic {:el/self {:type :checkbox :value false :label "Basic"}
                            :readout {:type :text :text "checked:false"}}}}
        (scan/nest [["checkbox--basic" {:type :checkbox :value false :label "Basic"}]
                    ["checkbox--basic--readout" {:type :text :text "checked:false"}]]))))

  (testing "a later sibling still lands next to an existing leaf"
    (is
     (= {:row {:amount john :note h3}}
        (scan/nest [["row--amount" john]
                    ["row--note" h3]])))))

(deftest nest-accepts-custom-ignore
  (is
   (= {:gallery-thing h3}
      (scan/nest [["gallery-thing" h3]] (constantly false)))))
