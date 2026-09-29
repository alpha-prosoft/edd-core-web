(ns web.primitives.sdk.autocomplete
  "RawAutocomplete. el-id lands as id on the combobox <input>."
  (:require
   [web.primitives.sdk.core :as sdk])
  (:import
   [com.microsoft.playwright Page$GetByRoleOptions]
   [com.microsoft.playwright.options AriaRole]))

(defn- option [label]
  (.getByRole sdk/*page*
              AriaRole/OPTION
              (-> (Page$GetByRoleOptions.) (.setName label) (.setExact true))))

(defn- chip-root [el-id]
  (.locator (sdk/el el-id) "xpath=ancestor::div[contains(@class,'MuiAutocomplete-root')]"))

(defn search!
  "Type `text` into the input to filter the options."
  [el-id text]
  (.fill (sdk/el el-id) (str text)))

(defn open!
  [el-id]
  (.click (sdk/el el-id)))

(defn choose!
  "Pick the option with the given visible label.
   In multiple mode call it once per value. Waits until the option popup
   closes so the selection is committed before the caller reads state."
  [el-id label]
  (open! el-id)
  (.click (option label))
  (sdk/wait-hidden (option label)))

(defn visible-options
  "Labels in the open popup, after optional `search!`."
  [el-id]
  (open! el-id)
  (vec (.allInnerTexts (.getByRole sdk/*page* AriaRole/OPTION))))

(defn value
  "Text in the input (single mode)."
  [el-id]
  (.inputValue (sdk/el el-id)))

(defn chips
  "Labels of the selected chips (multiple mode)."
  [el-id]
  (vec (.allInnerTexts (.locator (chip-root el-id) ".MuiChip-label"))))

(defn clear!
  "Press the clear button."
  [el-id]
  (.hover (sdk/el el-id))
  (.click (.locator (chip-root el-id) ".MuiAutocomplete-clearIndicator")))
