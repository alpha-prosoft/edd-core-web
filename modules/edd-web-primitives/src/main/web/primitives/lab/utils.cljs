(ns web.primitives.lab.utils
  (:require
   ["@mui/material/index" :refer [CircularProgress TextField]]
   [reagent.core :as r]))

(defn autocomplete-text-field [input-params
                               {:keys [helper-text FormHelperTextProps label disabled required loading error variant]
                                :or   {label "" variant "standard"}}]
  [(r/adapt-react-class TextField)
   {:value               (-> input-params .-value)
    :label               label
    :variant             variant
    :disabled            disabled
    :FormHelperTextProps (if (some? FormHelperTextProps) FormHelperTextProps (-> input-params .-FormHelperTextProps))
    :helper-text         (if (some? helper-text) helper-text (-> input-params .-helperText))
    :required            required
    :error               error
    :fullWidth           (-> input-params .-fullWidth)
    :inputProps          (-> input-params .-inputProps)
    :InputProps
    (if loading
      {:end-adornment (r/as-element [:> CircularProgress {:size 20}])}
      (-> input-params .-InputProps))}])
