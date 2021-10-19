(ns web.devcards.select
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [reagent.core :as r]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawSelect]]))

(defcard-rg :select
  "## Select"
  (fn [data-atom _]
    (apply-stiles
     [RawSelect {:options   ["mn" "tsd" "full"]
                 :value     (:selected @data-atom)
                 :on-change #(swap! data-atom merge {:selected %})}]))
  (r/atom {:selected "mn"}))