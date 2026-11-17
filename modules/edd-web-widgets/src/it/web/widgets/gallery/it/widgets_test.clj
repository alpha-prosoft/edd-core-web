(ns web.widgets.gallery.it.widgets-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.button :as button]
   [web.primitives.sdk.core :as sdk]
   [web.primitives.sdk.dialog :as dialog]
   [web.primitives.sdk.select :as select]
   [web.primitives.sdk.snackbar :as snackbar]
   [web.primitives.testing.elements :as els])
  (:import
   [com.microsoft.playwright.options FilePayload]
   [java.nio.charset StandardCharsets]
   [java.nio.file Files Path]
   [java.nio.file.attribute FileAttribute]
   [java.util Base64]))

(def ^:private one-pixel-png
  "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=")

(defn- png-file ^Path []
  (let [path
        (Files/createTempFile "widgets-gallery" ".png" (make-array FileAttribute 0))]
    (Files/write path (.decode (Base64/getDecoder) ^String one-pixel-png) (make-array java.nio.file.OpenOption 0))
    path))

(defn- png-payload ^FilePayload [file-name]
  (FilePayload. file-name "image/png" (.decode (Base64/getDecoder) ^String one-pixel-png)))

(defn- text-payload ^FilePayload [file-name mime-type]
  (FilePayload. file-name mime-type (.getBytes "just some notes" StandardCharsets/UTF_8)))

(defn- choose-pictures! [& payloads]
  (.setInputFiles (sdk/el "folder-input-photos") ^"[Lcom.microsoft.playwright.options.FilePayload;" (into-array FilePayload payloads)))

(defn- pictures []
  (sdk/text (h/readout :folder :list)))

(defn- focused-testid []
  (.evaluate (sdk/page) "() => document.activeElement && document.activeElement.dataset.testid"))

(deftest folder-list-refuses-what-is-not-a-picture
  (with-gallery
    (step "open folder page" #(h/open-page! :folder))

    (step "a text file, also one named and typed as a png, is refused by name"
          (fn []
            (choose-pictures! (text-payload "notes.txt" "text/plain") (text-payload "fake.png" "image/png"))

            (is
             (sdk/eventually= (str "notes.txt is not a picture (JPEG, PNG, WebP or GIF). "
                                   "fake.png is not a picture (JPEG, PNG, WebP or GIF).")
                              #(when (sdk/present? "folder-refused-photos")
                                 (sdk/text "folder-refused-photos"))))

            (is
             (= "pictures:one,two,three"
                (pictures)))

            (is
             (zero?
              (.count (sdk/el "folder-uploading-photos"))))))))

(deftest folder-list-save-waits-for-uploads
  (with-gallery
    (step "open folder page" #(h/open-page! :folder))

    (step "uploads held by the storage are counted"
          (fn []
            (button/click! (h/el :folder :hold))
            (choose-pictures! (png-payload "first.png") (png-payload "second.png"))

            (is
             (sdk/eventually= "Uploading 2 pictures…"
                              #(sdk/text "folder-status-photos")))))

    (step "a save waits until they are stored, and keeps the order they were chosen in"
          (fn []
            (button/click! (h/el :folder :save))

            (is
             (= "saved:waiting"
                (sdk/text (els/testid :folder :list :saved))))

            (button/click! (h/el :folder :release))

            (is
             (sdk/eventually= "saved:one,two,three,first.png,second.png"
                              #(sdk/text (els/testid :folder :list :saved))))

            (is
             (= ""
                (sdk/text "folder-status-photos")))))))

(deftest folder-list-failure-names-the-file
  (with-gallery
    (step "open folder page" #(h/open-page! :folder))

    (step "a failed upload says which file"
          (fn []
            (button/click! (h/el :folder :fail))
            (choose-pictures! (png-payload "broken.png"))

            (is
             (sdk/eventually #(and (sdk/present? "folder-problem-photos")
                                   (str/includes? (sdk/text "folder-problem-photos")
                                                  "broken.png could not be uploaded."))))))))

(deftest folder-list-reorders-with-the-keyboard
  (with-gallery
    (step "open folder page" #(h/open-page! :folder))

    (step "move later keeps the focus on the moved picture"
          (fn []
            (.focus (sdk/el "folder-later-photos-1"))
            (sdk/press! "Enter")

            (is
             (sdk/eventually= "pictures:two,one,three"
                              pictures))

            (is
             (sdk/eventually= "folder-later-photos-2"
                              focused-testid))))

    (step "at the end the focus goes to move earlier"
          (fn []
            (sdk/press! "Enter")

            (is
             (sdk/eventually= "pictures:two,three,one"
                              pictures))

            (is
             (sdk/eventually= "folder-earlier-photos-3"
                              focused-testid))))

    (step "tile buttons are at least 40 px"
          (fn []
            (let [box
                  (.boundingBox (sdk/el "folder-remove-photos-1"))]
              (is
               (<= 40.0 (.width box)))

              (is
               (<= 40.0 (.height box))))))))

(deftest confirmation-dialog-proceeds-and-cancels
  (with-gallery
    (step "open confirmation dialog page" #(h/open-page! :confirmation-dialog))

    (step "proceed closes the dialog and runs the proceed callback"
          (fn []
            (button/click! (h/el :confirmation-dialog :basic))
            (dialog/wait-open! :confirmation-dialog)

            (is
             (= "Are you sure?"
                (dialog/title :confirmation-dialog)))

            (button/click! [:confirmation-dialog :proceed])
            (dialog/wait-closed! :confirmation-dialog)

            (is
             (= "answer:proceed"
                (sdk/text (h/readout :confirmation-dialog :basic))))))

    (step "cancel closes the dialog and runs the cancel callback"
          (fn []
            (button/click! (h/el :confirmation-dialog :basic))
            (dialog/wait-open! :confirmation-dialog)
            (button/click! [:confirmation-dialog :cancel])
            (dialog/wait-closed! :confirmation-dialog)

            (is
             (= "answer:cancel"
                (sdk/text (h/readout :confirmation-dialog :basic))))))))

(deftest content-switcher-swaps-content
  (with-gallery
    (step "open content switcher page" #(h/open-page! :content-switcher))

    (step "delete swaps in the confirmation, yes swaps back"
          (fn []
            (button/click! (h/el :content-switcher :basic))

            (is
             (sdk/becomes-visible? [:content-switcher :basic :yes]))

            (button/click! [:content-switcher :basic :yes])

            (is
             (sdk/becomes-visible? (h/el :content-switcher :basic)))))))

(deftest snackbar-alert-shows-message
  (with-gallery
    (step "open snackbar alert page" #(h/open-page! :snackbar-alert))

    (step "error button shows the error message"
          (fn []
            (button/click! (h/el :snackbar-alert :error))

            (is
             (= "error message"
                (snackbar/message :snackbar-alert)))

            (is
             (pos?
              (.count (sdk/within :snackbar-alert ".MuiAlert-colorError"))))))))

(deftest lang-select-changes-language
  (with-gallery
    (step "open language select page" #(h/open-page! :translation))

    (step "english is selected first"
          (fn []
            (is
             (= "EN. Language"
                (sdk/text (h/readout :translation :basic))))))

    (step "choosing de translates the label"
          (fn []
            (select/choose! (h/el :translation :basic) "de")

            (is
             (sdk/eventually= "DE. Sprache"
                              #(sdk/text (h/readout :translation :basic))))))))

(deftest image-widget-shows-uploaded-image
  (with-gallery
    (step "open upload page" #(h/open-page! :upload))

    (step "upload area renders its file input"
          (fn []
            (is
             (sdk/present? "gallery-upload-container"))

            (is
             (= "file"
                (sdk/attr "gallery-upload-field" "type")))))

    (step "a dropped image replaces the placeholder"
          (fn []
            (let [widget
                  (sdk/el (h/el :upload :image-widget))]
              (.setInputFiles (.locator widget "input[type=file]") (png-file))

              (is
               (sdk/eventually
                #(.startsWith (str (.getAttribute (.locator widget "img") "src")) "blob:"))))))))

(deftest login-bar-opens-login-dialog
  (with-gallery
    (step "open login page" #(h/open-page! :login))

    (step "logged out bar offers login and register"
          (fn []
            (is
             (sdk/becomes-visible? [:login-bar :login]))

            (is
             (sdk/becomes-visible? [:login-bar :register]))))

    (step "login opens the dialog; submit is enabled, an empty form says what is missing"
          (fn []
            (button/click! [:login-bar :login])
            (dialog/wait-open! [:login-bar :dialog])

            (is
             (= "Log in"
                (dialog/title [:login-bar :dialog])))

            (is
             (button/enabled? [:login-bar :submit-login]))

            (button/click! [:login-bar :submit-login])

            (is
             (sdk/eventually= "Enter an email address like name@example.com."
                              #(when (sdk/present? [:login-bar :error-message])
                                 (sdk/text [:login-bar :error-message]))))))

    (step "visibility toggle shows the password"
          (fn []
            (is
             (= "password"
                (sdk/attr [:login-bar :password] "type")))

            (button/click! [:login-bar :password-visibility])

            (is
             (sdk/eventually= "text"
                              #(sdk/attr [:login-bar :password] "type")))))

    (step "close button closes the dialog"
          (fn []
            (button/click! [:login-bar :close])
            (dialog/wait-closed! [:login-bar :dialog])

            (is
             (false?
              (dialog/open? [:login-bar :dialog])))))))
