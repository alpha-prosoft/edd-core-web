(ns web.devcards.slide
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawCheckbox RawSwitch RawSlide]]))

(defcard-rg :slide
  "## Slide"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:container true :item true}
       [RawCheckbox {:checked (:show @data-atom)
                     :on-change (fn [x] (swap! data-atom merge {:show (not (:show @data-atom))}))
                     :label "Show"}]]
      [RawGrid {:container true :item true}
       [RawSlide
        {:in (:show @data-atom)}
        [RawGrid {:container true :style {:background-color "#edeeee"}} "Hello there!"]]]]))

  (r/atom {:show false}))

(defcard-rg :double-slide
  "## Double Slide"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:container true :item true}
       [RawSwitch {:left-label  "Left"
                   :right-label "Right"
                   :left-value  :left
                   :right-value :right
                   :value       (:selected @data-atom)
                   :on-change #(swap! data-atom merge {:selected %})}]]
      [RawGrid {:container true :item true}
       [RawSlide
        {:in (= :right (:selected @data-atom))}
        [RawGrid {:container true :item true :xs true :style {:background-color "#edee99"}} "Right block"]]
       [RawSlide
        {:in (= :left (:selected @data-atom)) :direction "right"}
        [RawGrid {:container true :item true :xs true :style {:background-color "#edeeee"}} "Left block"]]]]))

  (r/atom {:selected :left}))

