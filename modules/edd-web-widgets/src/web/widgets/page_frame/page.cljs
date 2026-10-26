(ns web.widgets.page-frame.page
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.i18n :refer [tr]]
            [clojure.walk :refer [keywordize-keys]]
            [web.primitives.components :refer [RawAppBar RawButton RawDrawer RawGrid
                                               RawIconButton RawThemeProvider RawToolbar]]
            [web.primitives.icons.core :refer [KeyboardArrowRightIcon MenuIcon]]
            [web.widgets.snackbar-alert.views :as snackbar-alert.views]))

(defn menu-item
  [{:keys [classes]} item]
  (let [_lang @(rf/subscribe [::subs/selected-language])]
    [RawGrid {:on-click   #(rf/dispatch [::events/navigate item])
              :size 12}
     [RawButton {:on-click   #(rf/dispatch [::events/navigate item])
                 :key        (:key item)
                 :class-name (:nested classes)
                 :start-icon (r/as-element [KeyboardArrowRightIcon {}])}
      (tr item)]]))

(defn generate-classes [classes-with-theme]
  (update-keys
   classes-with-theme
   (fn [key] (str "." (name key)))))

(defn generate-container-classes [styles theme]
  (generate-classes (styles theme)))

(defn drawer
  [{:keys [classes menu drawer] :as ctx}]
  [RawDrawer {:open     @(rf/subscribe [::subs/drawer])
              :on-close #(rf/dispatch [::events/toggle-drawer])}

   [:div {:class-name (:drawer-list classes)
          :role       "presentation"}

    (if drawer
      (drawer ctx)
      [RawGrid {:container true :sx {:width "100%"}}
       [RawGrid {:size 12}]
       [RawGrid {:size 1}]
       [RawGrid {:size 10}
        (into
         [RawGrid {:container true
                   :spacing   1
                   :sx {:width "100%"}}]
         (map
          (fn [item]
            (menu-item ctx item))
          menu))]])]])

(defn page
  [{:keys [app-bar
           page-config
           hide-menu
           theme
           styles] :as ctx}]

  (if @(rf/subscribe [::subs/ready])
    (let [{:keys [xs
                  md]
           :or {xs 12
                md 6}}
          page-config]
      [RawGrid {:container  true
                :spacing 1
                :id (str ::page)
                :sx (merge {:justify-content "center"
                            :width "100%"}
                           (generate-container-classes styles theme))}
       [RawGrid {:size {:xs xs :md md}}
        [RawAppBar {:color      :secondary
                    :position   "static"}

         [RawToolbar {}
          (when-not (hide-menu)
            [RawIconButton {:edge     "start"
                            :bel      "Menu"
                            :on-click #(rf/dispatch [::events/toggle-drawer])}
             [MenuIcon]])

          (cond
            app-bar (app-bar)
            :else ":app-bar placeholder")]]]

       [RawGrid {:size 12}]
       [RawGrid {:size {:xs xs :md md}}
        (util/placeholder ctx)]
       (snackbar-alert.views/revoke-alert)
       (drawer ctx)])
    [RawGrid {:container  true
              :sx {:justify-content "center" :width "100%"}}
     [RawGrid {:size {:xs 6 :md 12}}
      "Loading..."]]))

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
