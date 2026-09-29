(ns web.primitives.sdk.menu
  "RawMenu / RawMenuItem. Items carry their own el-id."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- list-of [el-id]
  (sdk/within el-id "[role=menu]"))

(defn open?
  [el-id]
  (and (sdk/present? el-id)
       (.isVisible (list-of el-id))))

(defn wait-open!
  [el-id]
  (sdk/wait-visible (list-of el-id)))

(defn wait-closed!
  [el-id]
  (sdk/wait-hidden (list-of el-id)))

(defn items
  "Visible labels of the menu items."
  [el-id]
  (wait-open! el-id)
  (vec (.allInnerTexts (sdk/within el-id "[role=menuitem]"))))

(defn choose!
  "Click the menu item carrying `item-el-id`."
  [item-el-id]
  (sdk/click! item-el-id))
