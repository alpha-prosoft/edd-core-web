(ns web.devcards.timeline
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawTypography RawTimelineDot RawGrid]]))

(defcard-rg :timeline-dot
  "## RawTimelineDot
            ##### Outside Timeline size is determined by :style adjusting padding, width and height"
  (apply-stiles
   [RawGrid {:container true :direction "column"}
    [RawGrid {:container true :align-items "center"}
     [RawTimelineDot
      {:color   "secondary"
       :variant "filled"
       :sx      {:width  "10px"
                 :height "10px"}}]
     [RawTypography {:style {:margin-left "8px"}}
      ":color      \"secondary\"\n     :variant    \"filled\""]]
    [RawGrid {:container true :align-items "center"}
     [RawTimelineDot
      {:color   "secondary"
       :variant "outlined"
       :sx      {:width  "10px"
                 :height "10px"}}]
     [RawTypography {:sx {:margin-left "8px"}}
      ":color      \"secondary\"\n     :variant    \"outlined\""]]
    [RawGrid {:container true :align-items "center"}
     [RawTimelineDot
      {:color   "primary"
       :variant "filled"
       :sx      {:width  "16px"
                 :height "16px"}}]
     [RawTypography {:style {:margin-left "8px"}}
      ":color      \"primary\"\n     :variant    \"filled\""]]
    [RawGrid {:container true :align-items "center"}
     [RawTimelineDot
      {:color   "primary"
       :variant "outlined"
       :style   {:width  "16px"
                 :height "16px"}}]
     [RawTypography {:style {:margin-left "8px"}}
      ":color       \"primary\"\n    :variant    \"outlined\""]]]))