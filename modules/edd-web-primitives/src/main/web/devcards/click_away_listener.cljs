(ns web.devcards.click-away-listener
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]
   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawTextField RawClickAwayListener]]))

(defcard-rg :click-away-listener
  "## Example changes text when clicking in and clicking away from the area "
  (fn [data-atom _]
    (apply-stiles
     [RawClickAwayListener
      {:mouseEvent  "onMouseDown"
       :touchEvent  false
       :onClickAway #(swap! data-atom merge {:text "You have clicked away"})}
      [RawTextField {:autoFocus true
                     :label "Text inside ClickAwayListener"
                     :variant "standard"
                     :value (:text @data-atom)
                     :on-click #(swap! data-atom merge {:text "Please click outside!"})
                     :InputProps {:disableUnderline true
                                  :readOnly true}}]]))
  (r/atom {:text "Please click outside!"}))