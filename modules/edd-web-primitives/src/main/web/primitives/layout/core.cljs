(ns web.primitives.layout.core
  (:require
   ["@mui/material/Box" :default Box]
   ["@mui/material/Grid" :default Grid]
   ["@mui/material/Stack" :default Stack]))

(defn RawGrid [{:keys [xs size]
                :as props}
               & children]
  (let [size (or size {:xs xs})]
    (into
     [:> Grid (-> props
                  (assoc :size size))]
     (for [child children]
       child))))

(defn RawBox [props & children]
  (into
   [:> Box props]
   (for [child children]
     child)))

(defn RawStack [props & children]
  (into
   [:> Stack props]
   (for [child children]
     child)))