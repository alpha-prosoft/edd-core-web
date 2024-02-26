(ns web.widgets.frame.page
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.i18n :refer [tr]]
            [clojure.walk :refer [keywordize-keys]]
            ["@mui/material/styles" :refer [createTheme, ThemeProvider]]
            ["@mui/material/AppBar" :default AppBar]
            ["@mui/material/Toolbar" :default Toolbar]
            ["@mui/material/Button" :default Button]
            ["@mui/material/IconButton" :default IconButton]
            ["@mui/material/Drawer" :default Drawer]
            ["@mui/material/Grid" :default Grid]
            ["@mui/icons-material/KeyboardArrowRight" :default KeyboardArrowRightIcon]
            ["@mui/icons-material/Menu" :default MenuIcon]
            [web.widgets.snackbar-alert.views :as snackbar-alert.views]))

(defn menu-item
  [{:keys [classes]} item]
  (let [_lang @(rf/subscribe [::subs/selected-language])]
    [:> Grid {:on-click   #(rf/dispatch [::events/navigate item])
              :item true
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
  [{:keys [app-bar
           page-config] :as ctx}]

  (if @(rf/subscribe [::subs/ready])
    (let [{:keys [xs
                  md]
           :or {xs 12
                md 6}}
          page-config]
      [:> Grid {:container  true
                :spacing 1
                :justify-content "center"}
       [:> Grid {:item       true
                 :xs xs
                 :md md}
        [:> AppBar {:color      :secondary
                    :position   "static"}

         [:> Toolbar
          [:> IconButton {:edge     "start"
                          :bel      "Menu"
                          :on-click #(rf/dispatch [::events/toggle-drawer])}
           [:> MenuIcon]]

          (cond
            app-bar (app-bar)
            :else ":app-bar placeholder")]]]

       [:> Grid {:item true
                 :xs 12}]
       [:> Grid {:item true
                 :xs xs
                 :md md}
        (util/placeholder ctx)]
       (snackbar-alert.views/revoke-alert)
       (drawer ctx)])
    [:> Grid {:container  true
              :justify-content "center"}
     [:> Grid {:item true
               :xs 6
               :md 12}
      "Loading..."]]))

(defn body
  "Initialize body with custom style"
  [{:keys [theme] :as ctx}]
  [:> ThemeProvider {:theme (createTheme (clj->js theme))}
   [:> (r/reactify-component
        (fn [props]
          (page
           (assoc
            ctx
            :classes (keywordize-keys
                      (:classes (js->clj props)))))))]])
