(ns web.devcards.box
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawBox]]))

(defcard-rg :box
  "## Box"
  [RawBox {:style {:text-align "center" :background-color "#ececec"}}
   "Box Wrapper"])