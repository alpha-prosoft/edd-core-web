(ns web.primitives.layout.core
  (:require ["@material-ui/core" :refer [Grid Box]]
            [reagent.core :as r]))

(defn RawGrid [props & children]
  (into
   [:> Grid props]
   (for [child children]
     child)))

(defn RawBox [props & children]
  (into
   [:> Box props]
   (for [child children]
     child)))