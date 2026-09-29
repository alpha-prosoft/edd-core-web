(ns web.gallery.it.display-test
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.display :as display]))

(deftest typography-variants-use-semantic-tags
  (with-gallery
    (step "open typography page" #(h/open-page! :typography))

    (step "each variant renders the expected tag and text"
          (fn []
            (doseq [[variant tag] {:h1 "h1" :h2 "h2" :h3 "h3" :body1 "p" :caption "span"}]

              (is
               (= tag
                  (display/tag (h/el :typography variant))))

              (is
               (= (str (name variant) " sample")
                  (display/text (h/el :typography variant)))))))))

(deftest avatar-shows-letter
  (with-gallery
    (step "open avatar page" #(h/open-page! :avatar))

    (step "letter avatar shows its letter"
          (fn []
            (is
             (= "A"
                (display/text (h/el :avatar :letter))))

            (is
             (true?
              (display/visible? (h/el :avatar :empty))))))))

(deftest badge-shows-count-including-zero
  (with-gallery
    (step "open badge page" #(h/open-page! :badge))

    (step "count and zero are both shown"
          (fn []
            (is
             (= "4"
                (display/badge-content (h/el :badge :count))))

            (is
             (= "0"
                (display/badge-content (h/el :badge :zero))))))))

(deftest chip-shows-label
  (with-gallery
    (step "open chip page" #(h/open-page! :chip))

    (step "chip label"
          (fn []
            (is
             (= "Chip"
                (display/chip-label (h/el :chip :basic))))))))

(deftest divider-is-rendered
  (with-gallery
    (step "open divider page" #(h/open-page! :divider))

    (step "divider is an hr"
          (fn []
            (is
             (= "hr"
                (display/tag (h/el :divider :basic))))))))

(deftest list-shows-items
  (with-gallery
    (step "open list page" #(h/open-page! :list))

    (step "items in order"
          (fn []
            (is
             (= ["Item one" "Item two"]
                (display/list-items (h/el :list :basic))))))))

(deftest table-shows-headers-and-rows
  (with-gallery
    (step "open table page" #(h/open-page! :table))

    (step "headers and body rows"
          (fn []
            (let [t (h/el :table :basic)]

              (is
               (= ["Name" "Age"]
                  (display/table-headers t)))

              (is
               (= [["Ann" "30"]]
                  (display/table-rows t))))))))

(deftest layout-renders
  (with-gallery
    (step "open layout page" #(h/open-page! :layout))

    (step "grid, box and stack are present with their content"
          (fn []
            (is
             (true?
              (display/present? (h/el :layout :grid))))

            (is
             (= "Box content"
                (display/text (h/el :layout :box))))

            (is
             (= "a\nb"
                (display/text (h/el :layout :stack))))))))

(deftest card-and-app-bar
  (with-gallery
    (step "open surfaces page" #(h/open-page! :surfaces))

    (step "card title and app bar are shown"
          (fn []
            (is
             (= "Card title"
                (display/card-title (h/el :surfaces :card))))

            (is
             (= "Title"
                (display/text (h/el :surfaces :app-bar))))))))

(deftest lab-components
  (with-gallery
    (step "open lab page" #(h/open-page! :lab))

    (step "alert severity, skeleton, timeline"
          (fn []
            (is
             (= "success"
                (display/alert-severity (h/el :lab :alert))))

            (is
             (= "All good"
                (display/text (h/el :lab :alert))))

            (is
             (true?
              (display/visible? (h/el :lab :skeleton))))

            (is
             (true?
              (display/present? (h/el :lab :timeline))))))))

(deftest tree-view-expands
  (with-gallery
    (step "open lab page" #(h/open-page! :lab))

    (step "expanding Root reveals Child"
          (fn []
            (let [tree (h/el :lab :tree)]

              (is
               (= ["Root"]
                  (display/tree-items tree)))

              (display/expand-tree-item! tree "Root")

              (is
               (= ["Root" "Child"]
                  (display/tree-items tree))))))))

(deftest progress-is-shown
  (with-gallery
    (step "open feedback page" #(h/open-page! :feedback))

    (step "circular progress is visible"
          (fn []
            (is
             (true?
              (display/progress? (h/el :feedback :progress))))))))

(deftest icons-render
  (with-gallery
    (step "open icons page" #(h/open-page! :icons))

    (step "icon grid is shown"
          (fn []
            (is
             (true?
              (display/visible? (h/el :icons :grid))))))))
