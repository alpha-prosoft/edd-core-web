(ns web.divider
  (:require
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawDivider]]))

(defcard-rg :divider "## Divider" [RawDivider])