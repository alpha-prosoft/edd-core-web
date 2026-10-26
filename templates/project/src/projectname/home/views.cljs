(ns projectname.home.views
  (:require [re-frame.core :as rf]
            [edd.i18n :as lang]
            [projectname.home.subs :as subs]
            [projectname.home.events :as events]
            [web.primitives.components :refer [RawButton RawGrid]]))

(defn main-panel
  [classes]
  [RawGrid {:container true}
   [RawGrid {:size 12}
    @(rf/subscribe [::subs/name])]
   [RawGrid {:size 12}
    [RawButton
     {:class-name (:page classes)
      :on-click   #(rf/dispatch [::events/click])}
     "Increment"]]
   [RawGrid {:size 12}
    (lang/tr :increments-count) @(rf/subscribe [::subs/clicks])]
   (into [RawGrid {:container true
                   :size      12}]
         (mapv
          (fn [i]
            [RawGrid {}
             [RawButton {:on-click #(rf/dispatch [::events/open (:id i)])}
              (:first-name i)]])
          @(rf/subscribe [::subs/items])))])
