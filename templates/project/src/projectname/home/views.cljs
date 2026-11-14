(ns projectname.home.views
  (:require [re-frame.core :as rf]
            [edd.i18n :as lang]
            [projectname.home.elements :as el]
            [projectname.home.subs :as subs]
            [projectname.home.events :as events]
            [web.primitives.components :refer [RawButton RawGrid RawTypography]]))

(defn main-panel
  []
  [RawGrid {:container true}
   [RawGrid {:size 12}
    [RawTypography {:el-id   el/title
                    :variant "h5"}
     @(rf/subscribe [::subs/name])]]
   [RawGrid {:size 12}
    [RawButton
     {:el-id    el/increment
      :on-click #(rf/dispatch [::events/click])}
     "Increment"]]
   [RawGrid {:size 12}
    [RawTypography {:el-id el/clicks}
     (str (lang/tr :increments-count) @(rf/subscribe [::subs/clicks]))]]
   (into [RawGrid {:container true
                   :size      12}]
         (mapv
          (fn [i]
            [RawGrid {}
             [RawButton {:el-id    (el/item (:id i))
                         :on-click #(rf/dispatch [::events/open (:id i)])}
              (:first-name i)]])
          @(rf/subscribe [::subs/items])))])
