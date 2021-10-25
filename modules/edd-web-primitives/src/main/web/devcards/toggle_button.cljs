(ns web.devcards.toggle-button
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawToggleButtonGroup RawToggleButton]]))

(defcard-rg :toggle-buttons
  "## Toggle Button"
  (fn [data-atom _]
    (apply-stiles [RawToggleButtonGroup
                   {:id        "button"
                    :orientation "horizontal"}
                   (RawToggleButton {:orientation "horizontal" :value :one :selected (= :one (:selected @data-atom))
                                     :on-click (fn [] (swap! data-atom merge {:selected :one}))}
                                    "One")
                   (RawToggleButton {:value :two :selected (= :two (:selected @data-atom))
                                     :on-click (fn [] (swap! data-atom merge {:selected :two}))} "Two")
                   (RawToggleButton {:value :three :selected (= :three (:selected @data-atom))
                                     :on-click (fn [] (swap! data-atom merge {:selected :three}))} "Three")]))

  (r/atom {:selected :one}))
