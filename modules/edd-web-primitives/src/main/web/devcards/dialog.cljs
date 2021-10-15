(ns web.devcards.dialog
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]
   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawDialog RawButton RawGrid]]))

(defcard-rg :dialog
  "## Dialog"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open dialog"]
      [RawDialog {:open (:open @data-atom)
                  :title "A dialog"}
       [RawButton
        {:on-click #(swap! data-atom merge {:open false})}
        "Close"]]]))
  (r/atom {:open false}))

(defcard-rg :dialog-with-actions
  "## Dialog with actions"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawButton
       {:on-click #(swap! data-atom merge {:open true})}
       "Open dialog"]
      [RawDialog {:open (:open @data-atom)
                  :title "A dialog"
                  :dividers true
                  :actions [RawButton
                            {:on-click #(swap! data-atom merge {:open false})}
                            "Close"]}
       "Content"]]))
  (r/atom {:open false}))
