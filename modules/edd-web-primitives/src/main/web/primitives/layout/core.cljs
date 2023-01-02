(ns web.primitives.layout.core
  (:require
   ["@mui/material/index" :refer [Box Grid Stack]]))

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

(defn RawStack [props & children]
  (into
   [:> Stack props]
   (for [child children]
     child)))