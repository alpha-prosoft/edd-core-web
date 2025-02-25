(ns web.circular-progress
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawCircularProgress]]))

(defcard-rg :circular-progress
  "## Progress"
  (apply-stiles
   [RawCircularProgress {}]))
