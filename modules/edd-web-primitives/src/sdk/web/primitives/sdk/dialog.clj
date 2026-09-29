(ns web.primitives.sdk.dialog
  "RawDialog. MUI renders the dialog in a portal; el-id lands as
   data-testid on the dialog root, so it is found wherever it is mounted."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- paper [el-id]
  (sdk/within el-id "[role=dialog]"))

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

(defn title
  [el-id]
  (.trim (.innerText (sdk/within el-id ".MuiDialogTitle-root"))))

(defn body-text
  [el-id]
  (.trim (.innerText (sdk/within el-id ".MuiDialogContent-root"))))

(defn close-with-escape!
  [el-id]
  (wait-open! el-id)
  (sdk/press! "Escape")
  (wait-closed! el-id))
