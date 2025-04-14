(ns web.primitives.utils.core
  (:require
   ["@mui/material/Zoom" :default Zoom]
   ["@mui/material/Slide" :default Slide]))

(defn RawSlide [{:keys [in direction timeout]
                 :or   {in false direction "left" timeout 500}}
                content]
  [:> Slide {:in            in
             :direction     direction
             :mountOnEnter  true
             :unmountOnExit true
             :timeout       timeout}
   [:div content]])

(defn RawZoom [{:keys [in] :or {in false} :as props} content]
  [:> Zoom (merge
            {:in in}
            props)
   [:div content]])
