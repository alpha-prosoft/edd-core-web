(ns web.primitives.data-display.core
  (:require

   ["@mui/material/Avatar" :default Avatar]
   ["@mui/material/Badge" :default Badge]
   ["@mui/material/Divider" :default Divider]
   ["@mui/material/Grid" :default Grid]
   ["@mui/material/List" :default List]
   ["@mui/material/ListItem" :default ListItem]
   ["@mui/material/ListItemButton" :default ListItemButton]
   ["@mui/material/ListItemText" :default ListItemText]
   ["@mui/material/ListItemIcon" :default ListItemIcon]
   ["@mui/material/ListSubheader" :default ListSubheader]
   ["@mui/material/Menu" :default Menu]
   ["@mui/material/MenuItem" :default MenuItem]
   ["@mui/material/MenuList" :default MenuList]

   ["@mui/material/Typography" :default Typography]
   ["@mui/material/Tooltip" :default Tooltip]
   ["@mui/material/Table" :default Table]
   ["@mui/material/TableBody" :default TableBody]
   ["@mui/material/TableCell" :default TableCell]
   ["@mui/material/TableContainer" :default TableContainer]
   ["@mui/material/TableHead" :default TableHead]
   ["@mui/material/TableRow" :default TableRow]
   ["@mui/material/Chip" :default Chip]
   [web.primitives.utils :as utils]))

(defn RawBadge [{:keys [color]
                 :or   {color "primary"}
                 :as   props}]
  [:> Badge (merge
             props
             {:color color}
             (when (= 0 (:badge-content props))
               {:badge-content "0"}))
   (:content props)])

(defn RawTooltip [{:keys [container-style no-grid arrow]
                   :as props
                   :or {no-grid false
                        arrow true}} content]
  [:> Tooltip
   (merge
    {:arrow arrow}
    (-> props
        (dissoc :container-style
                :no-grid)
        (utils/handle-paper-props-migration)))
   (if no-grid
     content
     [:> Grid {:sx (merge {:width "fit-content"}
                          container-style)}
      content])])

(defn RawTypography [props content]
  [:> Typography (utils/handle-root-styles-migration  props) content])

(defn RawList [props & children]
  (into [:> List (merge {:component "nav"} props)]
        (for [child children]
          child)))

(defn RawListItem [{:keys [button] :as props} & children]
  (if (true? button)
    (let [list-item-props (merge
                           {:disablePadding true}
                           (select-keys props [:align-items
                                               :dense
                                               :disable-gutters
                                               :disable-padding
                                               :divider
                                               :secondary-action]))
          list-item-button-props (-> props
                                     (dissoc :align-items
                                             :button
                                             :dense
                                             :disable-gutters
                                             :disable-padding
                                             :divider
                                             :secondary-action)
                                     (update :sx #(merge {:cursor "pointer"} %)))]
      [:> ListItem list-item-props
       (into [:> ListItemButton list-item-button-props]
             (for [child children]
               child))])
    (into [:> ListItem (dissoc props :button :disableTouchRipple)]
          (for [child children]
            child))))

(defn RawListItemText [props content]
  [:> ListItemText props content])

(defn RawListItemIcon [props content]
  [:> ListItemIcon props content])

(defn RawListSubheader [props content]
  [:> ListSubheader props content])

(defn RawDivider [props]
  [:> Divider props])

(defn RawMenu [{:keys [open] :or {open false} :as props} & children]
  (into [:> Menu (merge props {:open (boolean open)})]
        (for [child children]
          child)))

(defn RawMenuList [props & children]
  (into [:> MenuList props]
        (for [child children]
          child)))

(defn RawMenuItem [props content]
  [:> MenuItem props content])

(defn RawTableContainer [props & children]
  (into
   [:> TableContainer props]
   (for [child children]
     child)))

(defn RawTable [props & children]
  (into
   [:> Table props]
   (for [child children]
     child)))

(defn RawTableBody [props & children]
  (into
   [:> TableBody props]
   (for [child children]
     child)))

(defn RawTableHead [props & children]
  (into
   [:> TableHead props]
   (for [child children]
     child)))

(defn RawTableRow [props & children]
  (into
   [:> TableRow props]
   (for [child children]
     child)))

(defn RawTableCell [props & children]
  (into
   [:> TableCell props]
   (for [child children]
     child)))

(defn RawAvatar
  ([props content]
   [:> Avatar props content])
  ([props]
   [:> Avatar props]))

(defn RawChip [props]
  [:> Chip props])