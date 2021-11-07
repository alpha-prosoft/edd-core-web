(ns web.devcards.drawer
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawDrawer RawButton RawGrid]]
   [reagent.core :as r]))

(defcard-rg :drawer
  "## Drawer"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open Drawer"]
      [RawDrawer {:open     (:open @data-atom)
                  :on-close #(swap! data-atom merge {:open false})}
       [RawButton {:on-click #(swap! data-atom merge {:open false})} "Close"]]]))
  (r/atom {:open false}))