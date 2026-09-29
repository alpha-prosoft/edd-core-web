(ns web.primitives.sdk.snackbar
  "RawSnackbar. el-id lands as data-testid on the snackbar root, which only
   exists while it is open."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn open?
  [el-id]
  (sdk/visible? el-id))

(defn wait-open!
  [el-id]
  (sdk/wait-visible (sdk/el el-id)))

(defn wait-closed!
  [el-id]
  (sdk/wait-hidden (sdk/el el-id)))

(defn message
  [el-id]
  (wait-open! el-id)
  (sdk/text el-id))
