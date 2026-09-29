(ns web.gallery.it.values-test
  "Exercises the DOM value scanner: value-of detects a primitive's type and
   reads its value; scan walks a page and nests el-ids on the \"--\" separator."
  (:require
   [clojure.edn :as edn]
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.checkbox :as checkbox]
   [web.primitives.sdk.values :as values]))

(deftest value-of-detects-checkbox
  (with-gallery
    (step "open checkbox page" #(h/open-page! :checkbox))

    (step "value-of returns the checkbox boolean"
          (fn []
            (let [basic (h/el :checkbox :basic)]

              (is
               (false?
                (values/value-of basic)))

              (checkbox/check! basic)

              (is
               (true?
                (values/value-of basic))))))))

(deftest value-of-detects-prechecked
  (with-gallery
    (step "open checkbox page" #(h/open-page! :checkbox))

    (step "prechecked variant reads true"
          (fn []
            (is
             (true?
              (values/value-of (h/el :checkbox :checked))))))))

(defn- leaves [snapshot]
  (filter #(and (map? %) (contains? % :type))
          (tree-seq map? vals snapshot)))

(deftest scan-builds-map-of-checkbox-values
  (with-gallery
    (step "open checkbox page" #(h/open-page! :checkbox))

    (step "scan returns a nested map of typed readings"
          (fn []
            (let [snapshot
                  (values/scan {:scope "[data-page-id='checkbox']"})

                  checkboxes
                  (filter #(= :checkbox (:type %)) (leaves snapshot))]

              (is
               (seq checkboxes))

              (is
               (every? #(boolean? (:value %)) checkboxes))

              (is
               (some #(= "Accept" (:label %)) checkboxes)))))))

(deftest scan-reads-text-and-input-state
  (with-gallery
    (step "typography reports its visible text"
          (fn []
            (h/open-page! :typography)

            (is
             (= {:type :text :text "h3 sample"}
                (values/reading-of (h/el :typography :h3))))))

    (step "a prefilled text field reports value and label"
          (fn []
            (h/open-page! :text-field)

            (is
             (= {:type :input :value "hello" :label "Field"}
                (values/reading-of (h/el :text-field :prefilled))))))))

(deftest gallery-snapshot-button-shows-same-map
  (with-gallery
    (step "open checkbox section" #(h/open-page! :checkbox))

    (step "an example's El-ids button shows the map the SDK scan reads"
          (fn []
            (let [scope
                  "[data-page-id='checkbox'] [data-testid='variant-body-basic']"

                  sdk-snapshot
                  (values/scan {:scope scope})

                  shown
                  (h/show-snapshot! :basic)]

              (is
               (seq sdk-snapshot))

              (is
               (= sdk-snapshot
                  (edn/read-string shown))))))))
