(ns web.primitives.sdk.button
  "RawButton, RawFab, RawIconButton, RawChoiceButton.
   el-id lands as data-testid on the <button>."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn click!
  "Click the button."
  [el-id]
  (sdk/click! el-id))

(defn enabled?
  "True when the button can be clicked."
  [el-id]
  (sdk/enabled? el-id))

(defn label
  "Button text as passed to the component. MUI upper-cases button text
   with CSS; this returns the original casing."
  [el-id]
  (sdk/text-content el-id))
