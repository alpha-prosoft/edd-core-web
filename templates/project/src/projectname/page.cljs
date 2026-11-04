(ns projectname.page
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.i18n :refer [tr]]
            [web.primitives.components :refer [RawAppBar RawButton RawDrawer RawGrid
                                               RawIconButton RawThemeProvider RawToolbar]]
            [web.primitives.icons.core :refer [KeyboardArrowRightIcon MenuIcon]]))

(defn menu-item
  [item]
  (let [_lang @(rf/subscribe [::subs/selected-language])]
    [RawGrid {:size 12}
     [RawButton {:on-click   #(rf/dispatch [::events/navigate item])
                 :key        (:key item)
                 :start-icon (r/as-element [KeyboardArrowRightIcon {}])}
      (tr item)]]))

(defn drawer
  [{:keys [menu drawer] :as ctx}]
  [RawDrawer {:open     @(rf/subscribe [::subs/drawer])
              :on-close #(rf/dispatch [::events/toggle-drawer])}

   [:div {:style {:width 250}
          :role  "presentation"}

    (if drawer
      (drawer ctx)
      [RawGrid {:container true}
       [RawGrid {:size 12}]
       [RawGrid {:size 1}]
       [RawGrid {:size 10}
        (into
         [RawGrid {:container true
                   :spacing   1}]
         (map menu-item menu))]])]])

(defn page
  [{:keys [app-bar] :as ctx}]
  (if @(rf/subscribe [::subs/ready])
    [:div
     (drawer ctx)
     [RawGrid {:container true}
      [RawGrid {:size 12}
       [RawAppBar {:position "static"}
        [RawToolbar {}
         [RawIconButton {:edge       "start"
                         :aria-label "Menu"
                         :on-click   #(rf/dispatch [::events/toggle-drawer])}
          [MenuIcon]]

         (cond
           app-bar (app-bar)
           :else ":app-bar placeholder")]]]

      [RawGrid {:container true}
       (util/placeholder ctx)]]]
    [RawGrid {:container true} "Loading"]))

(defn body
  [{:keys [theme] :as ctx}]
  [RawThemeProvider {:theme theme}
   [page ctx]])
