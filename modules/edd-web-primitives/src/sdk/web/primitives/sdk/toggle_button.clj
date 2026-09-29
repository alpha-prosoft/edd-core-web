(ns web.primitives.sdk.toggle-button
  "RawToggleButtonGroup / RawToggleButton. el-id lands as data-testid on
   the group; buttons are found by their value."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- button [el-id value]
  (sdk/within el-id (str "button[value='" value "']")))

(defn choose!
  [el-id value]
  (.click (button el-id value)))

(defn pressed?
  [el-id value]
  (= "true" (.getAttribute (button el-id value) "aria-pressed")))

(defn selected
  "Values of all pressed buttons."
  [el-id]
  (vec (.evaluateAll (sdk/within el-id "button[aria-pressed=true]")
                     "els => els.map(e => e.value)")))
