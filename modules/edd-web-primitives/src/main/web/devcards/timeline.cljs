(ns web.devcards.timeline
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.data-display.core :refer [RawTypography]]
   [web.primitives.lab.core :refer [RawTimelineDot]]
   [web.primitives.layout.core :refer [RawGrid]]))

(defcard-rg timeline-dot
  "## RawTimelineDot
  ##### Outside Timeline size is determined by :style adjusting padding, width and height"
  [RawGrid {:container true :direction "column"}
   [RawGrid {:container "true"}
    [RawTimelineDot
     {:color      "secondary"
      :variant    "default"
      :style {:padding 0
              :width   4
              :height  4}}]
    [RawTypography {:style {:margin-left 8}} ":color      \"secondary\"\n     :variant    \"default\""]]
   [RawGrid {:container "true"}
    [RawTimelineDot
     {:color      "secondary"
      :variant    "outlined"
      :style {:padding 0
              :width   6
              :height  6}}]
    [RawTypography {:style {:margin-left 8}} ":color      \"secondary\"\n     :variant    \"outlined\""]]
   [RawGrid {:container "true"}
    [RawTimelineDot
     {:color      "primary"
      :variant    "default"
      :style {:padding 0
              :width   8
              :height  8}}]
    [RawTypography {:style {:margin-left 8}} ":color      \"primary\"\n     :variant    \"default\""]]
   [RawGrid {:container "true"}
    [RawTimelineDot
     {:color      "primary"
      :variant    "outlined"
      :style {:padding 0
              :width   12
              :height  12}}]
    [RawTypography {:style {:margin-left 8}} ":color      \"primary\"\n     :variant    \"outlined\""]]])