(ns web.devcards.slider
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawSlider]]))

(defcard-rg :slider
  "## Slider"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:container true :item true}
       [RawSlider {:value (:value @data-atom)
                   :color "secondary"
                   :min 0
                   :max 100
                   :on-change (fn [_ value] (swap! data-atom merge {:value value}))}]]
      [RawGrid {}]]))

  (r/atom {:value 30}))


