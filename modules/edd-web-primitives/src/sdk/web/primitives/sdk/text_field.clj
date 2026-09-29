(ns web.primitives.sdk.text-field
  "RawTextField, RawNumberField, RawPercentField.
   el-id lands as id on the native <input>."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn value
  "Current text shown in the input."
  [el-id]
  (.inputValue (sdk/el el-id)))

(defn- wait-value-stable!
  "Block until the input's value stops changing between two polls, so the
   onChange -> re-frame -> re-render chain triggered by typing/filling has
   settled before the caller reads dependent state."
  [el-id]
  (sdk/eventually
   (let [previous (atom ::init)]
     (fn []
       (let [current (value el-id)]
         (if (= current @previous)
           true
           (do (reset! previous current) false)))))))

(defn fill!
  "Replace the input's content with `text`, then wait for the value to settle."
  [el-id text]
  (.fill (sdk/el el-id) (str text))
  (wait-value-stable! el-id))

(defn clear!
  [el-id]
  (.fill (sdk/el el-id) "")
  (wait-value-stable! el-id))

(defn type!
  "Type `text` key by key into the input (triggers per-key handlers), then
   wait for the value to settle so per-key transforms have run."
  [el-id text]
  (.pressSequentially (sdk/el el-id) (str text))
  (wait-value-stable! el-id))

(defn blur!
  "Move focus away so on-blur handlers and formatting run."
  [el-id]
  (.blur (sdk/el el-id)))

(defn read-only?
  [el-id]
  (some? (sdk/attr el-id "readonly")))

(defn enabled?
  [el-id]
  (sdk/enabled? el-id))

(defn label
  "Text of the field's floating label."
  [el-id]
  (sdk/text (str (sdk/el-id->string el-id) "-label")))

(defn helper-text
  "Helper / error text under the field, or nil when none is shown."
  [el-id]
  (let [helper
        (sdk/el (str (sdk/el-id->string el-id) "-helper-text"))]
    (when (pos? (.count helper))
      (.innerText helper))))
