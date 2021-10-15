(ns web.devcards.switch
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.inputs.core :refer [RawSwitch]]
   [web.devcards.utils :refer [apply-stiles]]
   [reagent.core :as r]))

(defcard-rg switch
  "## Switch"
  (apply-stiles [RawSwitch {:value true}]))

(defcard-rg switch-disabled
  "## Switch disabled"
  (apply-stiles [RawSwitch {:value true :disabled true :label "Disabled Switch"}]))

(defcard-rg switch-with-label
  "## Switch with label"
  (apply-stiles [RawSwitch {:value true :label "Switch with label"}]))

(defcard-rg switch-with-label-placement-start
  "## Switch with label placement on the start"
  (apply-stiles [RawSwitch {:value false :label "Switch with label" :label-placement "start"}]))

(defcard-rg switch-with-two-variants
  "## Switch with two variants"
  (fn [data-atom _]
    (apply-stiles [RawSwitch {:left-label  "Variant one"
                              :right-label "Variant two"
                              :left-value  :one
                              :right-value :two
                              :value       (:selected @data-atom)
                              :on-change #(swap! data-atom merge {:selected %})}]))
  (r/atom {:selected :one}))
