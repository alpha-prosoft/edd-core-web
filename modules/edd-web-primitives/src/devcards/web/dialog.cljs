(ns web.dialog
  (:require
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]
   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawDialog RawButton RawGrid RawTextField]]))

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
       [RawGrid {:container true :spacing 2}
        [RawTextField {:label "Text field"}]
        [RawButton
         {:on-click #(swap! data-atom merge {:open false})}
         "Close"]]]]))

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
