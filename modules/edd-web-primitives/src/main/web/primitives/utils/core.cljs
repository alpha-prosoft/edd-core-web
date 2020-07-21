(ns web.primitives.utils.core
  (:require
   ["@material-ui/core" :refer [Slide]]))

(defn RawSlide [{:keys [in direction timeout]
                 :or   {in false direction "left" timeout 500}}
                content]
  [:> Slide {:in            in
             :direction     direction
             :mountOnEnter  true
             :unmountOnExit true
             :timeout       timeout}
   content])

