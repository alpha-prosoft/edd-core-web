(ns web.box
  (:require
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawBox]]))

(defcard-rg :box
  "## Box"
  [RawBox {:sx {:text-align "center" :background-color "#ececec"}}
   "Box Wrapper"])