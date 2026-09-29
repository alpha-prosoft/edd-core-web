(ns web.gallery.app
  (:require
   [reagent.dom.client :as rdomc]
   [web.primitives.gallery.core :refer [EddComponentGallery]]))

(defonce root* (atom nil))

(defn mount! []
  (let [container   (.getElementById js/document "app")
        react-root  (or @root* (reset! root* (rdomc/create-root container)))]
    (rdomc/render react-root [EddComponentGallery {}])))

(defn ^:export init []
  (mount!))
