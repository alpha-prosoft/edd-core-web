(ns web.devcards.linear-progress
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [EddLinearProgress]]))

(defcard-rg :linear-progress-5
  "## Linear Progress 5 seconds"
  (apply-stiles [EddLinearProgress {:time 5}]))

(defcard-rg :linear-progress-10
  "## Linear Progress 10 seconds"
  (apply-stiles [EddLinearProgress {:time 10}]))

(defcard-rg :linear-progress-30
  "## Linear Progress 30 seconds"
  (apply-stiles [EddLinearProgress {:time 30}]))

