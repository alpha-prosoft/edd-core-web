(ns web.primitives.utils.core
  (:require
   [reagent.core :as r]

   ["@mui/material/Zoom" :default Zoom]
   ["@mui/material/index" :refer [Slide Grid]]))

(defn RawSlide [{:keys [in direction timeout]
                 :or   {in false direction "left" timeout 500}}
                content]
  (r/as-element [:> Slide {:in            in
                           :direction     direction
                           :mountOnEnter  true
                           :unmountOnExit true
                           :timeout       timeout}
                 [:> Grid {:container true} content]]))

(defn RawZoom [{:keys [in] :or {in false} :as props} content]
  [:> Zoom (merge
            {:in in}
            props)
   [:div content]])
