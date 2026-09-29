(ns web.primitives.sdk.switch
  "RawSwitch. Behaves like a checkbox (el-id lands on the native input,
   role=switch). For two-value switches `on?` means the right value."
  (:require
   [web.primitives.sdk.checkbox :as checkbox]))

(defn on? [el-id]
  (checkbox/checked? el-id))

(defn toggle! [el-id]
  (checkbox/toggle! el-id))

(defn turn-on! [el-id]
  (checkbox/check! el-id))

(defn turn-off! [el-id]
  (checkbox/uncheck! el-id))

(defn enabled? [el-id]
  (checkbox/enabled? el-id))
