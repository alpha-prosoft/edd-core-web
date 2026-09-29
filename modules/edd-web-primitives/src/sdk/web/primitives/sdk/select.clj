(ns web.primitives.sdk.select
  "RawSelect and RawFormSelect.
   RawSelect: el-id is the combobox itself.
   RawFormSelect: the combobox is el-id + \"-select\"."
  (:require
   [web.primitives.sdk.core :as sdk])
  (:import
   [com.microsoft.playwright Page$GetByRoleOptions]
   [com.microsoft.playwright.options AriaRole]))

(defn- combobox [el-id]
  (let [own
        (sdk/el el-id)

        form-select
        (sdk/el (str (sdk/el-id->string el-id) "-select"))]
    (if (pos? (.count own))
      own
      form-select)))

(defn- option [label]
  (.getByRole sdk/*page*
              AriaRole/OPTION
              (-> (Page$GetByRoleOptions.) (.setName label) (.setExact true))))

(defn open!
  "Open the options popup."
  [el-id]
  (.click (combobox el-id)))

(defn selected
  "Visible text of the currently selected option."
  [el-id]
  (.trim (.innerText (combobox el-id))))

(defn choose!
  "Open the select and pick the option with the given visible label, waiting
   until the selection settles to that label."
  [el-id label]
  (sdk/settle
   (fn []
     (open! el-id)
     (.click (option label))
     (sdk/wait-hidden (option label)))
   #(selected el-id)
   label))

(defn options
  "Visible labels of all options (opens and closes the popup)."
  [el-id]
  (open! el-id)
  (let [items
        (.getByRole sdk/*page* AriaRole/OPTION)

        labels
        (vec (.allInnerTexts items))]
    (sdk/press! "Escape")
    labels))
