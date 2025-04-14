(ns web.tooltip
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawTooltip RawButton]]))

(defcard-rg :tooltip
  "## Tooltip"
  (apply-stiles [RawTooltip
                 {:title "Tooltip text"}

                 [RawButton {} "Tooltip"]]))

(defcard-rg :tooltip-with-delays
  "## Tooltip with delays"
  (apply-stiles [RawTooltip
                 {:title "Tooltip text"
                  :enterDelay 1000
                  :leaveDelay 10000
                  :enterNextDelay 1000
                  :sx {:width "auto"}}

                 [RawButton {} "Tooltip"]]))
