(ns projectname.page
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.i18n :refer [tr]]
            [clojure.walk :refer [keywordize-keys]]
            [web.primitives.components :refer [RawAppBar RawButton RawDrawer RawGrid
                                               RawIconButton RawThemeProvider RawToolbar]]
            [web.primitives.icons.core :refer [KeyboardArrowRightIcon MenuIcon]]))

(defn menu-item
  [{:keys [classes]} item]
  (let [_lang @(rf/subscribe [::subs/selected-language])]
    [RawGrid {:size 12}
     [RawButton {:on-click   #(rf/dispatch [::events/navigate item])
                 :key        (:key item)
                 :class-name (:nested classes)
                 :start-icon (r/as-element [KeyboardArrowRightIcon {}])}
      (tr item)]]))

(defn drawer
  [{:keys [classes menu drawer] :as ctx}]
  [RawDrawer {:open     @(rf/subscribe [::subs/drawer])
              :on-close #(rf/dispatch [::events/toggle-drawer])}

   [:div {:class-name (:drawer-list classes)
          :role       "presentation"}

    (if drawer
      (drawer ctx)
      [RawGrid {:container true}
       [RawGrid {:size 12}]
       [RawGrid {:size 1}]
       [RawGrid {:size 10}
        (into
         [RawGrid {:container true
                   :spacing   1}]
         (map
          (fn [item]
            (menu-item ctx item))
          menu))]])]])

(defn page
  [{:keys [classes _panels app-bar] :as ctx}]

  (if @(rf/subscribe [::subs/ready])
    [:div {:class-name (:root classes)}
     (drawer ctx)
     [RawGrid {:container  true
               :class-name (:root classes)}
      [RawGrid {:size       12
                :class-name (:header classes)}
       [RawAppBar {:class-name (:app-bar classes)
                   :position   "static"}

        [RawToolbar {}
         [RawIconButton {:edge     "start"
                         :bel      "Menu"
                         :on-click #(rf/dispatch [::events/toggle-drawer])}
          [MenuIcon]]

         (cond
           app-bar (app-bar)
           :else ":app-bar placeholder")]]]

      [RawGrid {:container true :class-name (:page-wrapper classes)}
       (util/placeholder ctx)]]]
    [RawGrid {:container true} "Loading"]))

(defn body
  "Initialize body with custom style"
  [{:keys [theme] :as ctx}]
  [RawThemeProvider {:theme theme}
   [:> (r/reactify-component
        (fn [props]
          (page
           (assoc
            ctx
            :classes (keywordize-keys
                      (:classes (js->clj props)))))))]])
