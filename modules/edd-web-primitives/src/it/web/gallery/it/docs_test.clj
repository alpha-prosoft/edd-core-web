(ns web.gallery.it.docs-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]))

(deftest every-variant-shows-non-empty-code
  (with-gallery
    (step "every variant on every page has non-empty code"
          (fn []
            (doseq [pid (h/page-ids)]
              (h/open-page! pid)

              (doseq [variant (h/variant-ids)]

                (is
                 (true?
                  (h/code-toggle? variant))
                 (str pid "/" variant ": has a code toggle"))

                (is
                 (not (str/blank? (h/show-code! variant)))
                 (str pid "/" variant ": code is not empty"))))))))

(deftest code-toggle-shows-and-hides
  (with-gallery
    (step "open button page" #(h/open-page! :button))

    (step "toggle reveals highlighted code, second click hides it"
          (fn []
            (is
             (false?
              (h/code-visible? :contained)))

            (is
             (str/includes? (h/show-code! :contained) "RawButton"))

            (is
             (true?
              (h/code-highlighted? :contained)))

            (h/toggle-code! :contained)

            (is
             (false?
              (h/code-visible? :contained)))))))

(deftest code-includes-demo-helper-source
  (with-gallery
    (step "open autocomplete page" #(h/open-page! :autocomplete))

    (step "helper definition is shown above its usage, without pprint commas"
          (fn []
            (let [code (h/show-code! :basic)]

              (is
               (str/includes? code "(defn single-select"))

              (is
               (str/includes? code "RawAutocomplete"))

              (is
               (not (str/includes? code ",\n"))))))))

(deftest props-panel-expands
  (with-gallery
    (step "open autocomplete page" #(h/open-page! :autocomplete))

    (step "large props list starts collapsed and expands on click"
          (fn []
            (is
             (= 0
                (h/props-row-count)))

            (h/expand-props!)

            (is
             (<= 10 (h/props-row-count)))

            (is
             (str/includes? (h/props-text) ":multiple"))))))

(deftest small-props-list-starts-open
  (with-gallery
    (step "open divider page" #(h/open-page! :divider))

    (step "short props table is visible without clicking"
          (fn []
            (is
             (pos? (h/props-row-count)))))))

(deftest code-state-is-per-section
  (with-gallery
    (step "open a code block in the button section"
          (fn []
            (h/open-page! :button)
            (h/show-code! :text)

            (is
             (= 1
                (h/open-code-blocks)))))

    (step "other sections keep their own code state"
          (fn []
            (h/open-page! :checkbox)

            (is
             (= 0
                (h/open-code-blocks)))))))

(deftest sidebar-scrolls-to-section
  (with-gallery
    (step "load gallery" #(h/load-gallery!))

    (step "sidebar is grouped and clicking a link scrolls to its section"
          (fn []
            (is
             (str/includes? (h/sidebar-text) "Inputs"))

            (is
             (str/includes? (h/sidebar-text) "Data display"))

            (h/click-sidebar-link! :slider)

            (is
             (true?
              (h/section-in-viewport? :slider)))

            (is
             (= "Slider"
                (h/page-title)))))))

(deftest every-page-has-docs-and-props
  (with-gallery
    (step "each page renders a description and a props panel"
          (fn []
            (doseq [pid (h/page-ids)]
              (h/open-page! pid)

              (is
               (true?
                (h/has-doc?))
               (str pid ": has doc"))

              (is
               (true?
                (h/has-props-panel?))
               (str pid ": has props panel")))))))
