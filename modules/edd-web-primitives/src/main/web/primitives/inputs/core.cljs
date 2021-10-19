(ns web.primitives.inputs.core
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            ["@mui/material/index" :refer [FormGroup FormControl FormLabel FormControlLabel
                                           Select Switch Button InputAdornment
                                           TextField Fab Zoom
                                           IconButton Checkbox RadioGroup Radio
                                           MenuItem InputLabel]]

            ["@mui/lab/LocalizationProvider" :default LocalizationProvider]
            ["@mui/lab/DatePicker" :default DatePicker]
            ["@date-io/luxon/build/index.esm.js" :default luxon]

            [web.primitives.layout.core :refer [RawGrid]]
            [web.primitives.inputs.utils :as utils]
            [web.primitives.inputs.model :as model]))

(defn RawTextField
  [{:keys [prefix suffix read-only]
    :or   {read-only false} :as props}]
  [utils/adapted-text-field
   (merge {:full-width true :color "primary" :variant "standard"}
          (dissoc props :transform-func)
          (when
           (or (some? (or prefix suffix)) read-only)
            {:InputProps
             (merge
              (when read-only
                {:disableUnderline true :readOnly true})

              (when (some? prefix)
                {:startAdornment (r/as-element
                                  [:> InputAdornment {:position "start"} prefix])})

              (when (some? suffix)
                {:endAdornment (r/as-element
                                [:> InputAdornment {:position "end"} suffix])}))})

          {:on-change (fn [event] (utils/handle-input-change event
                                                             (get props :on-change
                                                                  (fn [] (println "BLL")))
                                                             (get props :transform-func (fn [val] val))))})])

(defn RawSwitch
  [{:keys [label label-placement disabled left-label left-value right-label right-value value]
    :or   {label-placement "end" disabled false label ""} :as props}]
  (if (and (some? left-label) (some? right-label))
    (if (and (some? left-value) (some? right-value) (some? value))
      [:> FormControlLabel
       {:control
        (r/as-element
         [:> FormControlLabel
          {:control
           (r/as-element
            [:> Switch (merge
                        (utils/clean-switch-props props)
                        {:color      "secondary"
                         :class-name :selector
                         :checked    (= right-value value)
                         :on-change  #(utils/handle-switcher-change
                                       {:left-value  left-value
                                        :right-value right-value
                                        :value       value
                                        :callback    (get props :on-change
                                                          (fn [] (println "BLL")))})})])
           :label           left-label
           :style           {:margin-left  4
                             :margin-right -4}
           :disabled        disabled
           :label-placement "start"}])
        :label           right-label
        :disabled        disabled
        :label-placement label-placement}]
      (throw
       (js/Error. "If you want to use switcher with two labels you should provide :left-value :right-value and :value")))

    [:> FormControlLabel
     {:control         (r/as-element [:> Switch (merge {:color "primary"} (utils/clean-switch-props props))])
      :label           label
      :style           {:margin-right -4}
      :disabled        disabled
      :label-placement label-placement}]))

(defn RawSelect
  [{:keys [id options value on-change style required? variant]
    :or   {id        ::simple-select
           required? true
           variant   "standard"}}]
  (let [default (first options)]
    [:> Select {:id           id
                :variant      variant
                :on-change    (fn [^js event]
                                (on-change (.. event -target -value)))
                :value        (or value default)
                :defaultValue default
                :children     (for [option options]
                                (r/as-element [:> MenuItem {:key option :value option} option]))
                :required     required?
                :style        style}]))

(defn RawFormSelect
  [props]
  (let [id-pre (:id props)
        input-label (:input-label props)]
    [:> FormControl
     (merge
      {:id          (str id-pre "-form-control")
       :no-validate true
       :full-width  true}
      (dissoc props :id :input-label :render-value :on-change :value :children))
     [:> InputLabel input-label]
     [:> Select
      (merge
       {:id         (str id-pre "-select")
        :variant    "standard"
        :inputProps {:hidden true}}
       (dissoc props :id :input-label :full-width :required :disabled))]]))

; ChoiceButton will be replaced with material Chip.
(defn RawChoiceButton
  [props]
  [:> Button
   (merge
    {:style   {:font-size      "14px"
               :width          "112px"
               :height         "32px"
               :border-radius  "16px"
               :text-transform "none"
               :box-shadow     "none"}
     :variant "contained"}
    props)
   (:label props)])

(defn RawDatePicker
  [{:keys [id value label required error helper-text
           set-touched set-focused invalid? invalid-date-message]
    :or   {id                   ::date-picker
           required             false
           set-touched          (fn [] (print "set-touched"))
           invalid-date-message "Invalid date"}
    :as   props}]

  [:> LocalizationProvider
   {:dateAdapter luxon}
   [:> DatePicker
    (merge
     props
     {:input-format           "dd.MM.yyyy"
      :allowSameDateSelection true
      :mask                   "__.__.____"
      :label                  label
      :clearable              true
      :clear-text             "Clear"
      :value                  value
      :renderInput            (fn [p]
                                (r/as-element
                                 [RawTextField
                                  (merge
                                   (utils/jsx->clj p)
                                   {:helper-text helper-text
                                    :id          id
                                    :required    required
                                    :error       error
                                    :on-blur     #(comp (set-touched) (set-focused false))}
                                   (when invalid?
                                     {:error       true
                                      :helper-text invalid-date-message}))]))
      :on-close               #(comp (set-touched) (set-focused false))
      :on-change              #(utils/handle-date-picker-date-change
                                (partial (get props :on-change (fn [x] (println "BLL"))))
                                set-focused
                                %)})]])

(defn EddDatePicker
  [props]
  (r/with-let [date-picker-state-id (keyword (str ::date-picker-state- (str (random-uuid))))]
    (let [focused? @(rf/subscribe [::model/date-picker-focused? date-picker-state-id])
          touched? @(rf/subscribe [::model/date-picker-touched? date-picker-state-id])
          set-focused (fn [focused?] (rf/dispatch [::model/set-date-picker-focused date-picker-state-id focused?]))
          set-touched (fn [] (rf/dispatch [::model/set-date-picker-touched date-picker-state-id]))]

      [RawDatePicker
       (merge
        {:invalid?    (utils/invalid-date? touched? focused? props)
         :set-focused set-focused
         :set-touched set-touched}
        props)])))

(defn RawButton
  [props inner-text]
  [:> Button props inner-text])

(defn RawFab
  ([props icon]
   [:> Zoom {:in true :timeout 300}
    [:> Fab (merge {:color "primary"} props) icon]])

  ([props icon inner-text]
   [:> Zoom {:in true :timeout 300}
    [:> Fab (merge
             {:color   "primary"
              :variant "extended"}
             props) icon [RawGrid {:style {:margin-left 5}} inner-text]]]))

(defn RawIconButton
  [{:keys [size] :or {size "medium"} :as props} icon]
  (let [size (if (= size "small") 20 24)
        icon-props {:style {:width size :height size}}
        adj-size-icon (if (some? icon)
                        (conj icon icon-props)
                        [RawGrid icon-props])
        button-props (if (some? icon) props (conj props {:disabled true}))]
    [:> IconButton button-props adj-size-icon]))

(defn EddIconButton
  ([props icon]
   [RawIconButton props icon])

  ([props icon on-hover-icon]
   (r/with-let [icon-button-state-id (keyword (str "icon-button-state-" (str (random-uuid))))]
     (let [hovered? @(rf/subscribe [::model/icon-button-hovered? icon-button-state-id])]
       [RawIconButton
        (merge props
               {:on-mouse-enter #(rf/dispatch [::model/set-icon-button-hovered icon-button-state-id true])
                :on-mouse-leave #(rf/dispatch [::model/set-icon-button-hovered icon-button-state-id false])})
        (if hovered?
          on-hover-icon
          icon)]))))

(defn RawCheckbox
  [{:keys [label label-placement disabled on-change]
    :or   {label-placement "end" disabled false label ""} :as props}]
  [:> FormControlLabel
   {:control         (r/as-element
                      [:> Checkbox (merge {:color "secondary"}
                                          (-> props
                                              (dissoc :label-placement)
                                              (dissoc :on-change)))])

    :on-change on-change
    :label           label
    :disabled        disabled
    :label-placement label-placement}])

(defn RawRadioGroup
  [{:keys [label children default-value defaultValue row on-change]
    :or   {row false on-change #(print (-> % .-target .-value))}}]
  [:> FormControl {:on-change on-change}
   [:> FormLabel {} label]
   (into
    [:> RadioGroup {:row row :defaultValue (or default-value defaultValue)}]

    (for [item children]
      [:> FormControlLabel
       (merge
        {:control (r/as-element [:> Radio (merge {:color "secondary"})])}
        item)]))])
