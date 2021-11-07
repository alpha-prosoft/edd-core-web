(ns web.primitives.navigation.core
  (:require
   ["@mui/material/index" :refer [Tabs Tab Link Drawer SwipeableDrawer]]
   [reagent.core :as r]))

(def adapted-RawTabs (r/adapt-react-class Tabs))

(def adapted-RawTab (r/adapt-react-class Tab))

(defn RawTabs [props]
  (into
   [adapted-RawTabs (dissoc props :children)]
   (map (fn [item-props] (r/as-element [adapted-RawTab item-props])) (:tabs props))))

(defn RawLink [props content]
  [(r/adapt-react-class Link) props content])

(defn RawDrawer [props content]
  [:> Drawer props content])

(defn RawSwipeableDrawer [props content]
  [:> SwipeableDrawer props content])
