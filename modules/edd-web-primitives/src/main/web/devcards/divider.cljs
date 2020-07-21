(ns web.devcards.divider
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.data-display.core :refer [RawDivider]]))

(defcard-rg divider "## Divider" [RawDivider])