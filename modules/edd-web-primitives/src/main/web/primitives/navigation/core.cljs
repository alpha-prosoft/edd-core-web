(ns web.primitives.navigation.core
  (:require
   [reagent.core :as r]
   ["@mui/material/Tabs" :default Tabs]
   ["@mui/material/Tab" :default Tab]
   ["@mui/material/Link" :default Link]
   ["@mui/material/Drawer" :default Drawer]
   ["@mui/material/SwipeableDrawer" :default SwipeableDrawer]
   [web.primitives.utils :as utils]))

(def adapted-RawTabs (r/adapt-react-class Tabs))

(def adapted-RawTab (r/adapt-react-class Tab))

(defn RawTabs [props]
  (let [props (utils/handle-tab-indicator-props-migration props)]
    (into
     [adapted-RawTabs (utils/with-el-id-as-testid (dissoc props :children))]
     (map (fn [item-props] (r/as-element [adapted-RawTab item-props])) (:tabs props)))))

(defn RawLink [props content]
  [(r/adapt-react-class Link) (utils/with-el-id-as-testid props) content])

(defn RawDrawer [props content]
  [:> Drawer (utils/with-el-id-as-testid props) content])

(defn RawSwipeableDrawer [props content]
  [:> SwipeableDrawer
   (merge
    {:onOpen (fn [])}
    (utils/with-el-id-as-testid props))
   content])
