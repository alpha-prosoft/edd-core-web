(ns web.primitives.sdk.drawer
  "RawDrawer / RawSwipeableDrawer. el-id lands as data-testid on the drawer
   root, which is portalled and only present while open."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- paper [el-id]
  (sdk/within el-id ".MuiDrawer-paper"))

(defn open?
  [el-id]
  (and (sdk/present? el-id)
       (.isVisible (paper el-id))))

(defn wait-open!
  [el-id]
  (sdk/wait-visible (paper el-id)))

(defn wait-closed!
  [el-id]
  (sdk/wait-hidden (paper el-id)))

(defn content-text
  [el-id]
  (wait-open! el-id)
  (.trim (.innerText (paper el-id))))

(defn close-with-escape!
  [el-id]
  (wait-open! el-id)
  (sdk/press! "Escape")
  (wait-closed! el-id))
