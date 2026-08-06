(ns web.primitives.lab.utils
  (:require
   [goog.object :as gobj]
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
  ;; In particular, the native input ref from .slotProps.htmlInput is required by
  ;; useAutocomplete's click handler (it calls inputRef.current.focus()).
  ;; FormHelperTextProps goes into slotProps.formHelperText (MUI v9 TextField API).
  ;;
  ;; IMPORTANT: never call (clj->js x) on a CLJS map before (merge ...) –
  ;; clj->js produces a plain JS object, which is not ISeqable, causing a crash.
  (let [slot-props         (-> input-params .-slotProps)
        field-id           (-> input-params .-id)
        label-id           (when field-id (str "autocomplete-text-field-" field-id "-label"))
        ;; Support both MUI v9 params (.slotProps.*) and legacy params
        ;; (.InputProps / .inputProps / .InputLabelProps).
        base-input-slot    (or (when slot-props (-> slot-props .-input))
                               (gobj/get input-params "InputProps"))
        html-input-slot    (or (when slot-props (-> slot-props .-htmlInput))
                               (gobj/get input-params "inputProps"))
        input-label-slot   (or (when slot-props (-> slot-props .-inputLabel))
                               (gobj/get input-params "InputLabelProps"))
        input-label-slot*  (cond
                             (and input-label-slot label-id)
                             (js/Object.assign #js {} input-label-slot #js {:id label-id})

                             input-label-slot
                             input-label-slot

                             label-id
                             #js {:id label-id}

                             :else
                             nil)
        use-slot-props?    (some? slot-props)
        input-ref          (when html-input-slot (.-ref html-input-slot))
        html-input-slot*   (when html-input-slot
                             (let [slot (js/Object.assign #js {} html-input-slot)]
                               (js-delete slot "ref")
                               slot))
        ;; Merge the loading adornment into the existing JS slot object *shallowly*.
        ;; Avoid js->clj/clj->js here because MUI/React props can contain cyclic objects.
        merged-input-slot  (if (and loading base-input-slot)
                             (js/Object.assign
                              #js {}
                              base-input-slot
                              #js {:endAdornment (r/as-element
                                                  [:<>
                                                   [:> CircularProgress {:size 20}]
                                                   (.-endAdornment base-input-slot)])})
                             base-input-slot)]
    [:> TextField
     (cond-> {:id          field-id
              :inputRef    input-ref
              :label       label
              :size        (-> input-params .-size)
              :variant     variant
              :disabled    (if (some? disabled) disabled (-> input-params .-disabled))
              :required    required
              :error       error
              :fullWidth   (-> input-params .-fullWidth)
              :helper-text (if (some? helper-text) helper-text (-> input-params .-helperText))}
       use-slot-props?
       (assoc :slotProps
              (cond-> {:input      merged-input-slot
                       :htmlInput  html-input-slot*
                       :inputLabel input-label-slot*}
                ;; FormHelperTextProps is already a CLJS map; pass it directly
                ;; into slotProps.formHelperText (MUI v9 replaces the direct prop).
                (some? FormHelperTextProps)
                (assoc :formHelperText FormHelperTextProps)))

       (not use-slot-props?)
       (assoc :InputProps merged-input-slot
              :inputProps html-input-slot*
              :InputLabelProps input-label-slot*)

       (and (not use-slot-props?) (some? FormHelperTextProps))
       (assoc :FormHelperTextProps FormHelperTextProps))]))
