(ns web.devcards.button
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.inputs.core :refer [RawButton]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg button
  "## Button"
  (apply-stiles [RawButton
                 {:id "button"
                  :on-click (fn [] (print "on-click"))}
                 "Click me"]))

(defcard-rg contained-button
  "## Contained Button "
  (apply-stiles [RawButton
                 {:id "button"
                  :variant "contained"
                  :color "primary"
                  :on-click (fn [] (print "on-click"))}
                 "Click me"]))

(defcard-rg outlined-button-large
  "## Outlined Large Button"
  (apply-stiles [RawButton
                 {:id "button"
                  :variant "outlined"
                  :size "large"
                  :on-click (fn [] (print "on-click"))}
                 "Click me"]))

