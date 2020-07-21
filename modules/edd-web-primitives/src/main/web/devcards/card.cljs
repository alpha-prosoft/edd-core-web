(ns web.devcards.card
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]

   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]
   ["@material-ui/core" :refer [Grid]]

   [web.primitives.surfaces.core :refer [RawCard]]))

(defcard-rg card
  "## Card"
  (apply-stiles [RawCard
                 {:header        {:title     "Header"
                                  :subheader "Subheader"
                                  :action    "action"}
                  :actions       [:> Grid {:container true} "actions"]}

                 [:> Grid {:container true :style {:background-color "#ececec"}}
                  [:> Grid {:item true}
                   "Content"]]]))
