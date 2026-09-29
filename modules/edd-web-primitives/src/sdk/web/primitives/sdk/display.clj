(ns web.primitives.sdk.display
  "Read-only queries for display components: typography, badge, chip,
   avatar, alert, list, table, tree view, card, backdrop, progress, skeleton."
  (:require
   [clojure.string :as str]
   [web.primitives.sdk.core :as sdk]))

(defn text
  "Visible text of any element carrying el-id."
  [el-id]
  (sdk/text el-id))

(defn text=?
  "Wait until the element's visible text equals `expected` (or the SDK timeout
   passes) and return the final text. Use for read-after-act assertions where a
   re-frame / React re-render updates a readout: (is (= v (display/text=? id v)))."
  [el-id expected]
  (sdk/eventually= expected #(sdk/text el-id)))

(defn visible?
  "Visible right now."
  [el-id]
  (sdk/visible? el-id))

(defn becomes-visible?
  "True once the element is visible. Use after an action that shows it
   (MUI animates Zoom, Slide, Fade, Backdrop)."
  [el-id]
  (sdk/becomes-visible? el-id))

(defn becomes-hidden?
  "True once the element is hidden or removed. Use after an action that
   hides it; waits for exit transitions to finish."
  [el-id]
  (sdk/becomes-hidden? el-id))

(defn present?
  [el-id]
  (sdk/present? el-id))

(defn tag
  "HTML tag name, e.g. \"h1\" for RawTypography variant h1."
  [el-id]
  (str/lower-case (.evaluate (sdk/el el-id) "e => e.tagName")))

(defn badge-content
  "RawBadge: text of the badge bubble."
  [el-id]
  (str/trim (.innerText (sdk/within el-id ".MuiBadge-badge"))))

(defn chip-label
  "RawChip: chip text."
  [el-id]
  (sdk/text el-id))

(defn delete-chip!
  "RawChip with :on-delete: click the delete icon."
  [el-id]
  (.click (sdk/within el-id ".MuiChip-deleteIcon")))

(defn alert-severity
  "RawAlert: \"success\" | \"info\" | \"warning\" | \"error\"."
  [el-id]
  (let [classes
        (sdk/attr el-id "class")]
    (some (fn [s] (when (str/includes? classes (str "MuiAlert-color" (str/capitalize s))) s))
          ["success" "info" "warning" "error"])))

(defn list-items
  "RawList: text of each list item."
  [el-id]
  (mapv str/trim (.allInnerTexts (sdk/within el-id ".MuiListItem-root"))))

(defn table-headers
  "RawTable (via its container el-id): header cell texts."
  [el-id]
  (mapv str/trim (.allInnerTexts (sdk/within el-id "thead th"))))

(defn table-rows
  "RawTable: body rows as vectors of cell texts."
  [el-id]
  (let [rows
        (sdk/within el-id "tbody tr")]
    (vec
     (for [i (range (.count rows))]
       (mapv str/trim (.allInnerTexts (.locator (.nth rows i) "td")))))))

(defn tree-items
  "RawTreeView: labels of the visible tree items."
  [el-id]
  (mapv str/trim (.allInnerTexts (sdk/within el-id ".MuiTreeItem-label"))))

(defn expand-tree-item!
  "RawTreeView: click the item with the given label to expand it."
  [el-id label]
  (.click (.filter (sdk/within el-id ".MuiTreeItem-content")
                   (-> (com.microsoft.playwright.Locator$FilterOptions.)
                       (.setHasText label)))))

(defn card-title
  "RawCard: header title text."
  [el-id]
  (str/trim (.innerText (sdk/within el-id ".MuiCardHeader-title"))))

(defn progress?
  "RawCircularProgress: true while the progress indicator is shown."
  [el-id]
  (sdk/visible? el-id))
