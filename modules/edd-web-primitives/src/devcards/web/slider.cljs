(ns web.slider
  (:require
   [reagent.core :as r]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawSlider]]))

(defcard-rg :slider
  "## Slider"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:size 12}
       [RawSlider {:value (:value @data-atom)
                   :color "secondary"
                   :on-change #(swap! data-atom merge {:value %2})}]]]))

  (r/atom {:value 30}))