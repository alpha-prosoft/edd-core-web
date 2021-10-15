(ns web.primitives.utils.core
  (:require
   [reagent.core :as r]

   ["@mui/material/index" :refer [Slide Transition Grid]]))

(defn RawSlide [{:keys [in direction timeout]
                 :or   {in false direction "left" timeout 500}}
                content]
  (r/as-element [:> Slide {:in            in
                           :direction     direction
                           :mountOnEnter  true
                           :unmountOnExit true
                           :timeout       timeout}
                 [:> Grid {:container true} content]]))
