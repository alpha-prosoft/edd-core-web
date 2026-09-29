(ns web.primitives.sdk.checkbox
  "RawCheckbox and RawSwitch. el-id lands as id on the native
   <input type=checkbox>, so checked state is read from the input itself."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn checked?
  "True when the box / switch is on."
  [el-id]
  (.isChecked (sdk/el el-id)))

(defn toggle!
  "Click once, flipping the state, and wait until the state actually flips.
   Re-clicks if the first click raced a re-render and was dropped."
  [el-id]
  (let [before (checked? el-id)]
    (sdk/settle #(sdk/click! el-id)
                #(checked? el-id)
                (not before))))

(defn set-checked!
  "Drive to the given boolean state, verifying it settled."
  [el-id on?]
  (when (not= on? (checked? el-id))
    (sdk/settle #(sdk/click! el-id)
                #(checked? el-id)
                on?)))

(defn check!
  "Turn on. No-op when already on."
  [el-id]
  (set-checked! el-id true))

(defn uncheck!
  "Turn off. No-op when already off."
  [el-id]
  (set-checked! el-id false))

(defn enabled?
  [el-id]
  (sdk/enabled? el-id))
