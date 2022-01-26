(ns web.devcards.toggle-button
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawToggleButtonGroup RawToggleButton RawGrid]]))

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

(def button-style {:text-transform       "none"
                   :padding       "2px"
                   :width         "50px"
                   :border-radius "3px"})

(defcard-rg :toggle-buttons-with-styles
  "## Toggle Button with styles"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true :direction "column" :spacing 2}
      [RawGrid {:item true}
       [RawToggleButtonGroup
        {:id        "button"}
        (RawToggleButton {:value    :one :selected (= :one (:selected @data-atom))
                          :on-click (fn [] (swap! data-atom merge {:selected :one}))
                          :sx       button-style}
                         "One")]]
      [RawGrid {:item true}
       [RawToggleButtonGroup
        {:id        "button"
         :orientation "horizontal"}

        (RawToggleButton {:value    :two :selected (= :two (:selected @data-atom))
                          :on-click (fn [] (swap! data-atom merge {:selected :two}))
                          :sx       button-style}
                         "Two")]]
      [RawGrid {:item true}
       [RawToggleButtonGroup
        {:id        "button"
         :orientation "horizontal"}
        (RawToggleButton {:value    :three :selected (= :three (:selected @data-atom))
                          :disabled true
                          :on-click (fn [] (swap! data-atom merge {:selected :three}))
                          :sx       button-style}
                         "Three")]]]))

  (r/atom {:selected :one}))
