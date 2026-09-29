(ns web.gallery.it.interactions-test
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.accordion :as accordion]
   [web.primitives.sdk.button :as button]
   [web.primitives.sdk.checkbox :as checkbox]
   [web.primitives.sdk.dialog :as dialog]
   [web.primitives.sdk.display :as display]
   [web.primitives.sdk.drawer :as drawer]
   [web.primitives.sdk.menu :as menu]
   [web.primitives.sdk.snackbar :as snackbar]
   [web.primitives.sdk.tabs :as tabs]
   [web.primitives.sdk.text-editor :as text-editor]
   [web.primitives.sdk.toggle-button :as toggle-button]
   [web.primitives.sdk.tooltip :as tooltip]))

(deftest dialog-opens-and-closes
  (with-gallery
    (step "open feedback page" #(h/open-page! :feedback))

    (step "dialog opens with title and body, close button closes it"
          (fn []
            (let [dlg (h/el :feedback :dialog)]

              (button/click! (h/el :feedback :dialog-open))
              (dialog/wait-open! dlg)

              (is
               (= "Title"
                  (dialog/title dlg)))

              (is
               (= "Dialog body"
                  (dialog/body-text dlg)))

              (button/click! (h/el :feedback :dialog-close))
              (dialog/wait-closed! dlg)

              (is
               (false?
                (dialog/open? dlg))))))))

(deftest dialog-closes-with-escape
  (with-gallery
    (step "open feedback page" #(h/open-page! :feedback))

    (step "escape closes the dialog"
          (fn []
            (let [dlg (h/el :feedback :dialog)]

              (button/click! (h/el :feedback :dialog-open))
              (dialog/close-with-escape! dlg)

              (is
               (false?
                (dialog/open? dlg))))))))

(deftest snackbar-shows-message
  (with-gallery
    (step "open feedback page" #(h/open-page! :feedback))

    (step "snackbar appears with its message"
          (fn []
            (let [bar (h/el :feedback :snackbar)]

              (button/click! (h/el :feedback :snackbar-open))

              (is
               (= "Saved"
                  (snackbar/message bar))))))))

(deftest backdrop-opens-and-closes-on-click
  (with-gallery
    (step "open feedback page" #(h/open-page! :feedback))

    (step "backdrop shows a progress indicator and closes on click"
          (fn []
            (let [backdrop (h/el :feedback :backdrop)]

              (button/click! (h/el :feedback :backdrop-open))

              (is
               (true?
                (display/becomes-visible? backdrop)))

              (button/click! backdrop)

              (is
               (true?
                (display/becomes-hidden? backdrop))))))))

(deftest menu-picks-item
  (with-gallery
    (step "open menu page" #(h/open-page! :menu))

    (step "menu lists items and picking one records it"
          (fn []
            (let [m (h/el :menu :menu)]

              (button/click! (h/el :menu :open))

              (is
               (= ["A" "B"]
                  (menu/items m)))

              (menu/choose! (h/el :menu :item-b))
              (menu/wait-closed! m)

              (is
               (= "picked:B"
                  (display/text (h/readout :menu :menu)))))))))

(deftest tabs-change-selection
  (with-gallery
    (step "open navigation page" #(h/open-page! :navigation))

    (step "choose by label and by index"
          (fn []
            (let [t (h/el :navigation :tabs)]

              (is
               (= ["One" "Two" "Three"]
                  (tabs/labels t)))

              (is
               (= "One"
                  (tabs/selected t)))

              (tabs/choose! t "three")

              (is
               (= 2
                  (tabs/selected-index t)))

              (tabs/choose! t 1)

              (is
               (= "value:1"
                  (display/text (h/readout :navigation :tabs)))))))))

(deftest drawer-opens-and-closes
  (with-gallery
    (step "open navigation page" #(h/open-page! :navigation))

    (step "drawer opens with content and closes with escape"
          (fn []
            (let [d (h/el :navigation :drawer)]

              (button/click! (h/el :navigation :drawer-open))

              (is
               (= "Drawer content"
                  (drawer/content-text d)))

              (drawer/close-with-escape! d)

              (is
               (false?
                (drawer/open? d))))))))

(deftest accordion-expands-and-collapses
  (with-gallery
    (step "open surfaces page" #(h/open-page! :surfaces))

    (step "expand shows details, collapse hides them"
          (fn []
            (let [acc (h/el :surfaces :accordion)]

              (is
               (false?
                (accordion/expanded? acc)))

              (accordion/expand! acc)

              (is
               (= "Accordion details content"
                  (accordion/details-text acc)))

              (accordion/collapse! acc)

              (is
               (false?
                (accordion/expanded? acc))))))))

(deftest toggle-group-changes
  (with-gallery
    (step "open lab page" #(h/open-page! :lab))

    (step "choosing right presses it and releases left"
          (fn []
            (let [group (h/el :lab :toggle)]

              (is
               (= ["left"]
                  (toggle-button/selected group)))

              (toggle-button/choose! group "right")

              (is
               (true?
                (toggle-button/pressed? group "right")))

              (is
               (false?
                (toggle-button/pressed? group "left"))))))))

(deftest tooltip-shows-on-hover
  (with-gallery
    (step "open tooltip page" #(h/open-page! :tooltip))

    (step "hovering shows the tooltip text"
          (fn []
            (is
             (= "Hi there"
                (tooltip/text (h/el :tooltip :basic))))))))

(deftest icon-button-counts-clicks
  (with-gallery
    (step "open icon button page" #(h/open-page! :icon-button))

    (step "click increments"
          (fn []
            (button/click! (h/el :icon-button :medium))

            (is
             (= "clicks:1"
                (display/text (h/readout :icon-button :medium))))))))

(deftest fab-counts-clicks
  (with-gallery
    (step "open fab page" #(h/open-page! :fab))

    (step "click increments"
          (fn []
            (button/click! (h/el :fab :extended))

            (is
             (= "clicks:1"
                (display/text (h/readout :fab :extended))))))))

(deftest zoom-hides-and-shows
  (with-gallery
    (step "open transitions page" #(h/open-page! :transitions))

    (step "unchecking hides the zoomed box, checking shows it again"
          (fn []
            (let [box (h/el :transitions :zoom)
                  toggle (h/el :transitions :zoom-toggle)]

              (is
               (true?
                (display/visible? box)))

              (checkbox/uncheck! toggle)

              (is
               (true?
                (display/becomes-hidden? box)))

              (checkbox/check! toggle)

              (is
               (true?
                (display/becomes-visible? box))))))))

(deftest text-editor-types-in-edit-mode
  (with-gallery
    (step "open text editor page" #(h/open-page! :text-editor))

    (step "typing shows in the editor"
          (fn []
            (let [ed (h/el :text-editor :edit)]

              (is
               (true?
                (text-editor/editing? ed)))

              (text-editor/type! ed "Hello SDK")

              (is
               (= "Hello SDK"
                  (text-editor/text ed))))))))

(deftest text-editor-read-only-shows-content
  (with-gallery
    (step "open text editor page" #(h/open-page! :text-editor))

    (step "read only editor shows its html as text"
          (fn []
            (let [ed (h/el :text-editor :read-only)]

              (is
               (false?
                (text-editor/editing? ed)))

              (is
               (= "Read only"
                  (text-editor/text ed))))))))

(deftest text-editor-edit-and-save
  (with-gallery
    (step "open text editor page" #(h/open-page! :text-editor))

    (step "edit, type and save stores the new content"
          (fn []
            (let [ed (h/el :text-editor :with-save)]

              (is
               (false?
                (text-editor/editing? ed)))

              (is
               (= "Saved text"
                  (text-editor/text ed)))

              (text-editor/start-editing! ed)

              (is
               (true?
                (text-editor/editing? ed)))

              (text-editor/type! ed " and more")
              (text-editor/save! ed)

              (is
               (false?
                (text-editor/editing? ed)))

              (is
               (= "Saved text and more"
                  (text-editor/text ed))))))))

(deftest text-editor-discard-keeps-saved-content
  (with-gallery
    (step "open text editor page" #(h/open-page! :text-editor))

    (step "typing then discarding leaves the saved content unchanged"
          (fn []
            (let [ed (h/el :text-editor :with-save)]

              (text-editor/start-editing! ed)
              (text-editor/type! ed " draft")
              (text-editor/discard! ed)

              (is
               (false?
                (text-editor/editing? ed)))

              (is
               (= "Saved text"
                  (text-editor/text ed))))))))
