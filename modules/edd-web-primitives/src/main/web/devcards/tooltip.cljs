(ns web.devcards.tooltip
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.data-display.core :refer [RawTooltip]]
   [web.primitives.inputs.core :refer [RawButton]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg tooltip
  "## Tooltip"
  (apply-stiles [RawTooltip
                 {:title "Tooltip text"}

                 [RawButton {} "Tooltip"]]))

(defcard-rg tooltip-with-delays
  "## Tooltip with delays"
  (apply-stiles [RawTooltip
                 {:title "Tooltip text"
                  :enterDelay 1000
                  :leaveDelay 1000
                  :enterNextDelay 1000}

                 [RawButton {} "Tooltip"]]))

(defcard-rg tooltip-interactive
  "## Tooltip interactive"
  (apply-stiles [RawTooltip
                 {:title "Tooltip text"
                  :leaveDelay 1000
                  :interactive true}

                 [RawButton {} "Tooltip"]]))
