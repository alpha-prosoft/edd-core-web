(ns web.primitives.lab.core
  (:require
   [reagent.core :as r]

   ["@mui/x-tree-view" :refer [SimpleTreeView TreeItem]]
   ["@mui/lab/TimelineDot" :default TimelineDot]
   ["@mui/material/Autocomplete" :default Autocomplete]
   ["@mui/material/Alert" :default Alert]
   ["@mui/material/AlertTitle" :default AlertTitle]
   ["@mui/material/Skeleton" :default Skeleton]
   ["@mui/material/ToggleButtonGroup" :default ToggleButtonGroup]
   ["@mui/material/ToggleButton" :default ToggleButton]

   [web.primitives.lab.utils :refer [autocomplete-text-field]]))

(defn RawAutocomplete [{:keys [options getOptionSelected render-tags] :as props}]
  [:> Autocomplete (merge
                    {:options              (if options options [])
                     :renderInput          (fn [input-params] (r/as-element [autocomplete-text-field input-params props]))
                     :isOptionEqualToValue (fn [_option _value]
                                             (let [item (js->clj _option :keywordize-keys true)
                                                   value (js->clj _value :keywordize-keys true)]
                                               (= item value)))}
                        ;; These are consumed by autocomplete-text-field and should not leak
                        ;; into Autocomplete root props.
                    (dissoc props
                            :label
                            :variant
                            :required
                            :error
                            :options
                            :helperText
                            :FormHelperTextProps
                            :getOptionSelected
                            :helper-text)
                    (when (some? render-tags)
                      {:render-value render-tags})
                    (when (some? getOptionSelected)
                      {:isOptionEqualToValue getOptionSelected}))])

(defn RawAlert [{:keys [id severity title title-props] :as props} content]
  [:> Alert (merge {:severity  severity}
                   (when (some? id)
                     {:id id})
                   (dissoc props :title-props))
   (when (some? title)
     [:> AlertTitle (if (some? title-props) title-props {}) title])
   (r/as-element content)])

(defn RawSkeleton [props]
  [:> Skeleton props])

(defn RawToggleButtonGroup [props & children]
  (into
   [:> ToggleButtonGroup props]
   (for [child children]
     (r/as-element child))))

(defn RawToggleButton [props content]
  [:> ToggleButton props content])

(defn RawTimelineDot [{:keys [variant] :as props} & children]
  (into
   [:> TimelineDot (merge
                    props
                    (when (= "default" variant)
                      {:variant "filled"}))]
   (for [child children]
     (r/as-element child))))

(defn RawSimpleTreeView [props & children]
  (into
   [:> SimpleTreeView props]
   (for [child children]
     child)))

(defn RawTreeItem [props & children]
  (into
   [:> TreeItem props]
   (for [child children]
     child)))