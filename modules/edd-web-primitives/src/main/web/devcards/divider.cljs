(ns web.devcards.divider
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawDivider]]))

(defcard-rg :divider "## Divider" [RawDivider])