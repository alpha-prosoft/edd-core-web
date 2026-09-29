(ns web.primitives.sdk.tabs
  "RawTabs. el-id lands as data-testid on the tabs root."
  (:require
   [clojure.string :as str]
   [web.primitives.sdk.core :as sdk])
  (:import
   [com.microsoft.playwright Locator$FilterOptions]
   [java.util.regex Pattern]))

(defn- tabs [el-id]
  (sdk/within el-id "[role=tab]"))

(defn- exact-label
  "Case-insensitive whole-text match. Playwright hands the pattern to the
   browser, so regex metacharacters are escaped here (JS has no \\Q..\\E)."
  [label]
  (Pattern/compile (str "^\\s*" (str/replace (str label) #"[.*+?^${}()|\[\]\\]" "\\\\$0") "\\s*$")
                   Pattern/CASE_INSENSITIVE))

(defn labels
  "Labels of all tabs, in order (as passed to the component)."
  [el-id]
  (mapv str/trim (.allTextContents (tabs el-id))))

(defn selected
  "Label of the selected tab (as passed to the component)."
  [el-id]
  (str/trim (.textContent (sdk/within el-id "[role=tab][aria-selected=true]"))))

(defn selected-index
  [el-id]
  (.indexOf (labels el-id) (selected el-id)))

(defn choose!
  "Select a tab by its label (case-insensitive) or by index."
  [el-id label-or-index]
  (let [tab
        (if (number? label-or-index)
          (.nth (tabs el-id) (int label-or-index))
          (.filter (tabs el-id)
                   (-> (Locator$FilterOptions.)
                       (.setHasText (exact-label label-or-index)))))]
    (.click tab)
    (sdk/eventually #(= "true" (.getAttribute tab "aria-selected")))))
