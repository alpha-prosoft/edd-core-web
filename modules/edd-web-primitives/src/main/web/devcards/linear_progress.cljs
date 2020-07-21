(ns web.devcards.linear-progress
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.feedback.core :refer [EddLinearProgress]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg linear-progress
  "## Linear Progress"
  (apply-stiles [EddLinearProgress {:time 30}]))

