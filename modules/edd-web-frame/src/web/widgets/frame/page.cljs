(ns web.widgets.frame.page
  (:require [re-frame.core :as rf]
            [edd.subs :as subs]
            [edd.db :as edd-db]
            [reagent.core :as r]
            [edd.events :as events]
            [edd.util :as util]
            [edd.i18n :refer [tr]]
            [clojure.string :as str]
            [web.primitives.components :refer [RawAppBar RawButton RawDrawer RawGrid
                                               RawIconButton RawThemeProvider RawToolbar]]
            [web.primitives.icons.core :refer [KeyboardArrowRightIcon MenuIcon]]
            [web.widgets.snackbar-alert.views :as snackbar-alert.views]))

(rf/reg-sub
 ::env-prefix
 (fn [db]
   (let [hosted-zone
         (get-in db [::edd-db/config :PublicHostedZoneName])

         first-label
         (some-> hosted-zone (str/split #"\.") first str/lower-case)]
     (cond
       (nil? first-label)                  nil
       (str/starts-with? first-label "dev")  :dev
       (str/starts-with? first-label "test") :test
       :else                                 nil))))

(defn- menu-button-sx [env]
  (case env
    :dev  {:bgcolor   "error.main"
           :color     "common.white"
           :mr        1
           "&:hover"  {:bgcolor "error.dark"}}
    :test {:bgcolor   "info.main"
           :color     "common.white"
           :mr        1
           "&:hover"  {:bgcolor "info.dark"}}
    nil))

(defn menu-item
  [item]
  (let [_lang @(rf/subscribe [::subs/selected-language])]
    [RawGrid {:on-click   #(rf/dispatch [::events/navigate item])
              :size 12}
     [RawButton {:on-click   #(rf/dispatch [::events/navigate item])
                 :key        (:key item)
                 :start-icon (r/as-element [KeyboardArrowRightIcon {}])}
      (tr item)]]))

(defn drawer
  [{:keys [menu drawer] :as ctx}]
  [RawDrawer {:open     @(rf/subscribe [::subs/drawer])
              :on-close #(rf/dispatch [::events/toggle-drawer])}

   [:div {:role "presentation"}

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
         (map menu-item menu))]])]])

(defn page
  [{:keys [app-bar
           page-config
           hide-menu
           placeholder] :as ctx}]

  (if @(rf/subscribe [::subs/ready])
    (let [{:keys [xs
                  md]
           :or {xs 12
                md 6}}
          page-config]
      [RawGrid {:container  true
                :spacing 1
                :sx {:justify-content "center" :width "100%"}}
       [RawGrid {:size {:xs xs :md md}}
        [RawAppBar {:color      :secondary
                    :position   "static"}

         [RawToolbar {}
          (when-not (hide-menu)
            (let [env @(rf/subscribe [::env-prefix])]
              [RawIconButton (cond-> {:edge       "start"
                                      :aria-label "Menu"
                                      :on-click   #(rf/dispatch [::events/toggle-drawer])}
                               (some? (menu-button-sx env))
                               (assoc :sx (menu-button-sx env)))
               [MenuIcon]]))

          (cond
            app-bar (app-bar)
            :else ":app-bar placeholder")]]]

       [RawGrid {:size 12}]
       [RawGrid {:size {:xs xs :md md}}
        ((or placeholder util/placeholder) ctx)]
       (snackbar-alert.views/revoke-alert)
       (drawer ctx)])
    [RawGrid {:container  true
              :sx {:justify-content "center" :width "100%"}}
     [RawGrid {:size {:xs 6 :md 12}}
      "Loading..."]]))

(defn body
  [{:keys [theme] :as ctx}]
  [RawThemeProvider {:theme theme}
   [page ctx]])
