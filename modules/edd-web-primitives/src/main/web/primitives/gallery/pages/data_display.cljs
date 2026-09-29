(ns web.primitives.gallery.pages.data-display
  (:require
   [web.primitives.gallery.example :refer-macros [example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.data-display.core :refer [RawAvatar RawBadge RawChip RawDivider RawList RawListItem RawListItemText RawTable RawTableBody RawTableCell RawTableContainer RawTableHead RawTableRow RawTooltip]]
   [web.primitives.layout.core :refer [RawStack]]))

(def ^:private el-id-prop
  {:name :el-id :type "keyword | string" :default "—" :doc "Lands as data-testid."})

(defn register-avatar! []
  (page/register!
   {:id    :avatar
    :group "Data display"
    :title "Avatar"
    :doc   "A round image, icon or initials placeholder."
    :props [{:name :src     :type "string" :default "nil" :doc "Image URL."}
            {:name :alt     :type "string" :default "nil" :doc "Alt text."}
            {:name :variant :type "\"circular\" | \"rounded\" | \"square\"" :default "\"circular\"" :doc "Shape."}
            el-id-prop]
    :variants
    [(example {:id :letter :title "Letter"}
              [RawAvatar {:el-id (page/el :avatar :letter)} "A"])

     (example {:id :empty :title "Empty"}
              [RawAvatar {:el-id (page/el :avatar :empty)}])]}))

(defn register-badge! []
  (page/register!
   {:id    :badge
    :group "Data display"
    :title "Badge"
    :doc   "A small count or dot attached to its content. A zero count is still shown (MUI hides 0 by default)."
    :props [{:name :badge-content :type "node"   :default "—" :doc "Badge content; 0 is shown."}
            {:name :content       :type "node"   :default "—" :doc "Wrapped element."}
            {:name :color         :type "string" :default "\"primary\"" :doc "Theme color."}
            el-id-prop]
    :variants
    [(example {:id :count :title "Count"}
              [RawBadge {:el-id (page/el :badge :count) :badge-content 4 :content "Inbox"}])

     (example {:id :zero :title "Zero"}
              [RawBadge {:el-id (page/el :badge :zero) :badge-content 0 :content "Inbox"}])]}))

(defn register-chip! []
  (page/register!
   {:id    :chip
    :group "Data display"
    :title "Chip"
    :doc   "Compact element for tags, filters or selections."
    :props [{:name :label     :type "node"   :default "—" :doc "Chip text."}
            {:name :variant   :type "\"filled\" | \"outlined\"" :default "\"filled\"" :doc "Style."}
            {:name :on-delete :type "fn"     :default "nil" :doc "Shows a delete icon when set."}
            el-id-prop]
    :variants
    [(example {:id :basic :title "Basic"}
              [RawStack {:direction "row" :spacing 1}
               [RawChip {:el-id (page/el :chip :basic) :label "Chip"}]])]}))

(defn register-divider! []
  (page/register!
   {:id    :divider
    :group "Data display"
    :title "Divider"
    :doc   "A thin line that separates content."
    :props [{:name :orientation :type "\"horizontal\" | \"vertical\"" :default "\"horizontal\"" :doc "Direction."}
            el-id-prop]
    :variants
    [(example {:id :basic :title "Basic"}
              [:div
               [:span "above"]
               [RawDivider {:el-id (page/el :divider :basic)}]
               [:span "below"]])]}))

(defn register-list! []
  (page/register!
   {:id    :list
    :group "Data display"
    :title "List"
    :doc   "Vertical list of items. Set `:button true` on an item to make it clickable."
    :props [{:name :button :type "boolean" :default "false" :doc "RawListItem: render as a clickable ListItemButton."}
            {:name :dense  :type "boolean" :default "false" :doc "Compact spacing."}
            el-id-prop]
    :variants
    [(example {:id :basic :title "Basic"}
              [RawList {:el-id (page/el :list :basic)}
               [RawListItem {} [RawListItemText {} "Item one"]]
               [RawListItem {} [RawListItemText {} "Item two"]]])]}))

(defn register-table! []
  (page/register!
   {:id    :table
    :group "Data display"
    :title "Table"
    :doc   "Thin wrappers over the MUI table parts. All children are passed through."
    :props [el-id-prop]
    :variants
    [(example {:id :basic :title "Basic"}
              [RawTableContainer {:el-id (page/el :table :basic)}
               [RawTable {}
                [RawTableHead {}
                 [RawTableRow {} [RawTableCell {} "Name"] [RawTableCell {} "Age"]]]
                [RawTableBody {}
                 [RawTableRow {} [RawTableCell {} "Ann"] [RawTableCell {} "30"]]]]])]}))

(defn register-tooltip! []
  (page/register!
   {:id    :tooltip
    :group "Data display"
    :title "Tooltip"
    :doc   "Shows a label on hover. Content is wrapped in a Grid unless `:no-grid` is set."
    :props [{:name :title           :type "node"    :default "—"     :doc "Tooltip text."}
            {:name :arrow           :type "boolean" :default "true"  :doc "Show the arrow."}
            {:name :no-grid         :type "boolean" :default "false" :doc "Do not wrap content in a Grid."}
            {:name :container-style :type "map"     :default "nil"   :doc "sx for the wrapping Grid."}
            {:name :placement       :type "string"  :default "\"bottom\"" :doc "Position."}
            el-id-prop]
    :variants
    [(example {:id :basic :title "Basic"}
              [RawTooltip {:el-id (page/el :tooltip :basic) :title "Hi there"} "Hover me"])]}))

(defn register! []
  (register-avatar!)
  (register-badge!)
  (register-chip!)
  (register-divider!)
  (register-list!)
  (register-table!)
  (register-tooltip!))
