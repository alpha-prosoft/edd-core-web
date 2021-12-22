(ns projectname.home.views
  (:require [re-frame.core :as rf]
            [edd.i18n :as lang]
            [projectname.home.subs :as subs]
            [projectname.home.events :as events]

            ["@mui/material/Grid" :default Grid]
            ["@mui/material/Button" :default Button]))

(defn main-panel
  [classes]
  [:> Grid {:container true
            :item      true}
   [:> Grid {:item true
             :xs   12}
    @(rf/subscribe [::subs/name])]]
  [:> Grid {:item true
            :xs   12}
   [:> Button
    {:class-name (:page classes)
     :on-click   #(rf/dispatch [::events/click])}
    "Increment"]
   [:> Grid {:item true
             :xs   12}
    (lang/tr :increments-count) @(rf/subscribe [::subs/clicks])]
   (into [:> Grid {:item      true
                   :container true
                   :xs        12}]
         (mapv
          (fn [i]
            [:> Grid {:item true}
             [:> Button {:on-click #(rf/dispatch [::events/open (:id i)])}
              (:first-name i)]])
          @(rf/subscribe [::subs/items])))])







