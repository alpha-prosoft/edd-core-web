(ns web.widgets.gallery.main
  (:require
   [reagent.dom.client :as rdomc]
   [web.widgets.gallery.core :refer [WidgetsGallery]]))

(defonce root* (atom nil))

(defn ^:export init []
  (let [container  (.getElementById js/document "app")
        react-root (or @root* (reset! root* (rdomc/create-root container)))]
    (rdomc/render react-root [WidgetsGallery {}])))
