(ns web.devcards.radio
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawRadioGroup]]
   [web.devcards.utils :refer [apply-stiles]]
   [reagent.core :as r]))

(defcard-rg :radio
  "## Radio"
  (apply-stiles [RawRadioGroup {:label "Simple radio"
                                :defaultValue :one
                                :children [{:value :one
                                            :label "One"}
                                           {:value :two
                                            :label "Two"}
                                           {:value :tree
                                            :label "Tree"}]}]))

(defcard-rg :radio-in-row
  "## Radio in row"
  (apply-stiles [RawRadioGroup {:label "Simple radio"
                                :row true
                                :defaultValue :one
                                :children [{:value :one
                                            :label "One"}
                                           {:value :two
                                            :label "Two"}
                                           {:value :tree
                                            :label "Tree"}]}]))

(defcard-rg :radio-with-disabled-item
  "## Radio with disabled item"
  (apply-stiles [RawRadioGroup {:row true
                                :defaultValue :one
                                :children [{:value :one
                                            :label "One"}
                                           {:value :two
                                            :label "Two"}
                                           {:value :tree
                                            :disabled true
                                            :label "Tree"}]}]))

(defcard-rg :radio-controlled
  "## Radio Controlled"
  (fn [data-atom]
    (apply-stiles [RawRadioGroup {:label "Simple radio"
                                  :value (:selected @data-atom)
                                  :on-change #(swap! data-atom merge {:selected (-> % .-target .-value)})
                                  :children [{:value :one
                                              :label "One"}
                                             {:value :two
                                              :label "Two"}
                                             {:value :tree
                                              :label "Tree"}]}]))
  (r/atom {:selected :one}))