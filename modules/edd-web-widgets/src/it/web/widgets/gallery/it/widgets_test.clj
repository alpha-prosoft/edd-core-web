(ns web.widgets.gallery.it.widgets-test
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.button :as button]
   [web.primitives.sdk.core :as sdk]
   [web.primitives.sdk.dialog :as dialog]
   [web.primitives.sdk.select :as select]
   [web.primitives.sdk.snackbar :as snackbar]
   [web.primitives.sdk.text-field :as text-field])
  (:import
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

    (step "login opens the dialog with the submit button disabled"
          (fn []
            (button/click! [:login-bar :login])
            (dialog/wait-open! [:login-bar :dialog])

            (is
             (= "Login"
                (dialog/title [:login-bar :dialog])))

            (is
             (false?
              (button/enabled? [:login-bar :submit-login])))))

    (step "valid credentials enable submit"
          (fn []
            (text-field/fill! [:login-bar :username] "user@example.com")
            (text-field/fill! [:login-bar :password] "Passw0rdX")

            (is
             (sdk/eventually #(button/enabled? [:login-bar :submit-login])))))

    (step "visibility toggle shows the password"
          (fn []
            (is
             (= "password"
                (sdk/attr [:login-bar :password] "type")))

            (button/click! [:login-bar :password-visibility])

            (is
             (sdk/eventually= "input"
                              #(sdk/attr [:login-bar :password] "type")))))

    (step "close button closes the dialog"
          (fn []
            (button/click! [:login-bar :close])
            (dialog/wait-closed! [:login-bar :dialog])

            (is
             (false?
              (dialog/open? [:login-bar :dialog])))))))
