(ns web.primitives.sdk.date-picker
  "EddDatePicker / RawDatePicker.

   MUI v9 renders a sectioned field: el-id lands on a hidden input and the
   visible value lives in Day / Month / Year spinbutton sections. These
   functions hide that so tests only deal with \"dd.MM.yyyy\" strings."
  (:require
   [clojure.string :as str]
   [web.primitives.sdk.core :as sdk]))

(defn- field [el-id]
  (.locator (sdk/el el-id) "xpath=ancestor::div[contains(@class,'MuiPickersInputBase-root')]"))

(defn- section [el-id aria-label]
  (.locator (field el-id) (str "[role=spinbutton][aria-label='" aria-label "']")))

(defn- read-only-input? [el-id]
  (zero? (.count (field el-id))))

(defn value
  "Displayed value as \"dd.MM.yyyy\" (placeholders like \"DD\" when empty)."
  [el-id]
  (if (read-only-input? el-id)
    (.inputValue (sdk/el el-id))
    (str/replace (.innerText (.locator (field el-id) ".MuiPickersSectionList-root"))
                 #"\s+" "")))

(defn blank?
  "True when no date has been entered (sections show DD.MM.YYYY)."
  [el-id]
  (= "DD.MM.YYYY" (value el-id)))

(defn type-date!
  "Type digits into the field starting at the day section.
   `digits` like \"15052024\". Optional `delay-ms` between keys (default 50)
   simulates human typing, which matters for the day-first regression."
  ([el-id digits]
   (type-date! el-id digits 50))
  ([el-id digits delay-ms]
   (.click (section el-id "Day"))
   (sdk/type-keys! digits delay-ms)))

(defn set-date!
  "Type a \"dd.MM.yyyy\" date."
  ([el-id ddmmyyyy]
   (set-date! el-id ddmmyyyy 50))
  ([el-id ddmmyyyy delay-ms]
   (type-date! el-id (str/replace ddmmyyyy #"\D" "") delay-ms)))

(defn clear!
  "Select every section and delete it."
  [el-id]
  (.click (section el-id "Day"))
  (dotimes [_ 3]
    (sdk/press! "Backspace")
    (sdk/press! "ArrowRight")))

(defn blur!
  "Tab out of the field so validation runs."
  [_el-id]
  (sdk/press! "Tab"))

(defn open-calendar!
  [el-id]
  (sdk/click! (str (sdk/el-id->string el-id) ":openPickerButton")))

(defn helper-text
  "Helper / validation text under the field, or nil. Polls so validation
   triggered by blur has time to render the helper element."
  [el-id]
  (let [read (fn []
               (let [helper
                     (.locator (sdk/el el-id)
                               "xpath=ancestor::div[contains(@class,'MuiFormControl-root')][1]//*[contains(@class,'MuiFormHelperText-root')]")]
                 (when (pos? (.count helper))
                   (.innerText helper))))]
    (sdk/eventually read)))

(defn error?
  "True when the field is rendered in its error state. Polls so validation
   triggered by blur has time to apply the error class."
  [el-id]
  (boolean
   (sdk/eventually
    #(pos? (.count (.locator (sdk/el el-id)
                             "xpath=ancestor::div[contains(@class,'MuiFormControl-root')][1]//*[contains(@class,'Mui-error')]"))))))
