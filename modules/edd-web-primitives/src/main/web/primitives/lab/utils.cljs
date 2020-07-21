(ns web.primitives.lab.utils
  (:require
   ["@material-ui/core" :refer [CircularProgress TextField]]
   [reagent.core :as r]))

(defn autocomplete-text-field [params props]
  (let [helper-text (:helper-text props)
        FormHelperTextProps (:FormHelperTextProps props)]
    (do
      [(r/adapt-react-class TextField)
       {:value      (-> params .-value)
        :label      (:label props)
        :disabled (:disabled props)
        :FormHelperTextProps (if (some? FormHelperTextProps) FormHelperTextProps (-> params .-FormHelperTextProps))
        :helper-text (if (some? helper-text) helper-text (-> params .-helperText))
        :required (:required props)
        :fullWidth  (-> params .-fullWidth)
        :inputProps  (-> params .-inputProps)
        :InputProps
        (if (:loading props)
          {:end-adornment  (r/as-element [:> CircularProgress {:size 20}])}
          (-> params .-InputProps))}])))
