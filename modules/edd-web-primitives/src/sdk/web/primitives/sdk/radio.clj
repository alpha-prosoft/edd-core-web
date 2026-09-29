(ns web.primitives.sdk.radio
  "RawRadioGroup. el-id lands as data-testid on the radiogroup."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- radio [el-id value]
  (sdk/within el-id (str "input[type=radio][value='" value "']")))

(defn selected
  "Value of the checked option, or nil."
  [el-id]
  (let [checked
        (sdk/within el-id "input[type=radio]:checked")]
    (when (pos? (.count checked))
      (.getAttribute checked "value"))))

(defn choose!
  "Select the option whose value is `value`, waiting until it is checked."
  [el-id value]
  (sdk/settle #(.check (radio el-id value))
              #(selected el-id)
              value))

(defn values
  "All option values in order."
  [el-id]
  (vec (.evaluateAll (sdk/within el-id "input[type=radio]")
                     "els => els.map(e => e.value)")))
