(ns web.swipeable-drawer
  (:require
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawSwipeableDrawer RawButton RawGrid]]
   [reagent.core :as r]))

(defcard-rg :drawer
  "## Drawer"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open Drawer"]
      [RawSwipeableDrawer {:open     (:open @data-atom)
                           :on-close #(swap! data-atom merge {:open false})}
       [RawButton {:on-click #(swap! data-atom merge {:open false})} "Close"]]]))
  (r/atom {:open false}))
