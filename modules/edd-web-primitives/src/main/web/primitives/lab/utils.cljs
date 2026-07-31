(ns web.primitives.lab.utils
  (:require
   [reagent.core :as r]
   ["@mui/material/CircularProgress" :default CircularProgress]
   ["@mui/material/TextField" :default TextField]))

(defn autocomplete-text-field [input-params
                               {:keys [helper-text FormHelperTextProps label disabled required loading error variant]
                                :or   {label "" variant "standard"}}]
  ;; MUI v9 Autocomplete wires the field via slotProps in the renderInput params:
  ;;   .slotProps.input      – Input wrapper: ref, className, adornments, mouse handlers
  ;;   .slotProps.htmlInput  – Native <input>: value, onChange, aria-*, keyboard handlers
  ;;   .slotProps.inputLabel – InputLabel: htmlFor, id, etc.
  ;; These must be forwarded unchanged so Autocomplete can control focus / value.
  ;; FormHelperTextProps goes into slotProps.formHelperText (MUI v9 TextField API).
  ;;
  ;; IMPORTANT: never call (clj->js x) on a CLJS map before (merge ...) –
  ;; clj->js produces a plain JS object, which is not ISeqable, causing a crash.
  (let [slot-props        (-> input-params .-slotProps)
        base-input-slot   (when slot-props (-> slot-props .-input))
        html-input-slot   (when slot-props (-> slot-props .-htmlInput))
        input-label-slot  (when slot-props (-> slot-props .-inputLabel))
        ;; Merge the loading adornment into the existing JS slot object *shallowly*.
        ;; Avoid js->clj/clj->js here because MUI/React props can contain cyclic objects.
        merged-input-slot (if (and loading base-input-slot)
                            (js/Object.assign
                             #js {}
                             base-input-slot
                             #js {:endAdornment (r/as-element [:> CircularProgress {:size 20}])})
                            base-input-slot)]
    [:> TextField
     {:id          (-> input-params .-id)
      :label       label
      :variant     variant
      :disabled    disabled
      :required    required
      :error       error
      :fullWidth   (-> input-params .-fullWidth)
      :helper-text (if (some? helper-text) helper-text (-> input-params .-helperText))
      :slotProps   (cond-> {:input      merged-input-slot
                            :htmlInput  html-input-slot
                            :inputLabel input-label-slot}
                     ;; FormHelperTextProps is already a CLJS map; pass it directly
                     ;; into slotProps.formHelperText (MUI v9 replaces the direct prop).
                     (some? FormHelperTextProps)
                     (assoc :formHelperText FormHelperTextProps))}]))
