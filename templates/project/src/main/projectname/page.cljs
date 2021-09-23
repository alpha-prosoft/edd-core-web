(ns projectname.page
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.i18n :refer [tr]]
            [clojure.walk :refer [keywordize-keys]]
            ["@mui/material/styles" :refer [createTheme, ThemeProvider]]
            ["@mui/styles" :refer [withStyles]]
            ["@mui/material/AppBar" :default AppBar]
            ["@mui/material/Toolbar" :default Toolbar]
            ["@mui/material/Typography" :default Typography]
            ["@mui/material/Button" :default Button]
            ["@mui/material/IconButton" :default IconButton]
            ["@mui/material/Drawer" :default Drawer]
            ["@mui/material/List" :default List]
            ["@mui/material/ListItem" :default ListItem]
            ["@mui/material/ListItemText" :default ListItemText]
            ["@mui/material/ListItemIcon" :default ListItemIcon]
            ["@mui/material/ListSubheader" :default ListSubheader]
            ["@mui/material/Grid" :default Grid]
            ["@mui/material/Collapse" :default Collapse]
            ["@mui/icons-material/ChevronRight" :default ChevronRight]
            ["@mui/icons-material/StarBorder" :default StarBorder]
            ["@mui/icons-material/KeyboardArrowRight" :default KeyboardArrowRightIcon]
            ["@mui/material/FormControl" :default FormControl]
            ["@mui/material/FormHelperText" :default FormHelperText]
            ["@mui/material/Select" :default Select]
            ["@mui/material/InputLabel" :default InputLabel]
            ["@mui/material/MenuItem" :default MenuItem]
            ["@mui/icons-material/Menu" :default MenuIcon]))

(defn menu-item
  [{:keys [classes]} item]
  (let [lang @(rf/subscribe [::subs/selected-language])]
    [:> Grid {:item true
              :xs   12}
     [:> Button {:on-click   #(rf/dispatch [::events/navigate item])
                 :key        (:key item)
                 :class-name (:nested classes)
                 :start-icon (r/as-element [:> KeyboardArrowRightIcon])}
      (tr item)]]))

(defn drawer
  [{:keys [classes menu drawer] :as ctx}]
  [:> Drawer {:open     @(rf/subscribe [::subs/drawer])
              :on-close #(rf/dispatch [::events/toggle-drawer])}

   [:div {:class-name (:drawer-list classes)
          :role       "presentation"}

    (if drawer
      (drawer ctx)
      [:> Grid {:container true}
       [:> Grid {:item true
                 :xs   12}]
       [:> Grid {:item true
                 :xs   1}]
       [:> Grid {:item true
                 :xs   10}
        (into
          [:> Grid {:container true
                    :item      true
                    :spacing   1}]
          (map
            (fn [item]
              (menu-item ctx item))
            menu))]])]])

(defn page
  [{:keys [classes panels app-bar] :as ctx}]

  (if @(rf/subscribe [::subs/ready])
    [:div {:class-name (:root classes)}
     @(rf/subscribe [::subs/ready])
     (drawer ctx)
     [:> Grid {:container  true
               :class-name (:root classes)}
      [:> Grid {:item       true
                :xs         12
                :class-name (:header classes)}
       [:> AppBar {:class-name (:app-bar classes)
                   :position   "static"}

        [:> Toolbar
         [:> IconButton {:edge     "start"
                         :bel      "Menu"
                         :on-click #(rf/dispatch [::events/toggle-drawer])}
          [:> MenuIcon]]

         (cond
           app-bar (app-bar)
           :else ":app-bar placeholder")]]]

      [:> Grid {:container true :class-name (:page-wrapper classes)}
       (util/placeholder panels classes)]]]
    [:> Grid {:container true :item true} "Loading"]))


(defn with-custom-styles
  [{:keys [styles]} component]
  ((withStyles
     (fn [theme]
       (clj->js
         (styles theme)))) component))

(defn body
  [{:keys [theme] :as ctx}]
  "Initialize body with custom style"
  [:> ThemeProvider {:theme (createTheme (clj->js theme))}
   [:> (with-custom-styles
         ctx
         (r/reactify-component
           (fn [props]
             (page
               (assoc
                 ctx
                 :classes (keywordize-keys
                            (:classes (js->clj props))))))))]])