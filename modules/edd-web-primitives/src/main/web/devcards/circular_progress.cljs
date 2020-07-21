(ns web.devcards.circular-progress
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.feedback.core :refer [RawCircularProgress]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg circular-progress
  "## Progress"
  (apply-stiles [RawCircularProgress {}]))
