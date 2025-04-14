(ns web.slide
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawCheckbox RawSwitch RawSlide]]))

(defcard-rg :slide
  "## Slide"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:size 12}
       [RawCheckbox {:checked (:show @data-atom)
                     :on-change (fn [] (swap! data-atom merge {:show (not (:show @data-atom))}))
                     :label "Show"}]]
      [RawGrid {:size 12}
       [RawSlide
        {:in (:show @data-atom)}
        [RawGrid {:container true
                  :sx {:background-color "#edeeee"}}
         "Hello there!"]]]]))

  (r/atom {:show false}))

(defcard-rg :double-slide
  "## Double Slide"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:size 12}
       [RawSwitch {:left-label  "Left"
                   :right-label "Right"
                   :left-value  :left
                   :right-value :right
                   :value       (:selected @data-atom)
                   :on-change #(swap! data-atom merge {:selected %})}]]
      [RawGrid {:size 12}
       [RawSlide
        {:in (= :right (:selected @data-atom))}
        [RawGrid {:container true
                  :sx {:background-color "#edee99"}}
         "Right block"]]
       [RawSlide
        {:in (= :left (:selected @data-atom)) :direction "right"}
        [RawGrid {:container true
                  :sx {:background-color "#edeeee"}}
         "Left block"]]]]))

  (r/atom {:selected :left}))

