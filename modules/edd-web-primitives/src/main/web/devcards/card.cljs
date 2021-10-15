(ns web.devcards.card
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]

   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawGrid RawCard]]))

(defcard-rg :card
  "## Card"
  (apply-stiles [RawCard
                 {:header        {:title     "Header"
                                  :subheader "Subheader"
                                  :action    "action"}
                  :actions       [RawGrid {:container true} "actions"]}

                 [RawGrid {:container true :style {:background-color "#ececec"}}
                  [RawGrid {:item true}
                   "Content"]]]))
