(ns web.devcards.circular-progress
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawCircularProgress]]))

(defcard-rg :circular-progress
  "## Progress"
  (apply-stiles
   [RawCircularProgress {}]))
