(ns web.primitives.inputs.core
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            ["@mui/material/index" :refer [FormControl FormLabel FormControlLabel
                                           Select Switch Button InputAdornment
                                           Fab Zoom
                                           IconButton Checkbox RadioGroup Radio
                                           MenuItem InputLabel]]

            ["@mui/lab/LocalizationProvider" :default LocalizationProvider]
            ["@mui/lab/DesktopDatePicker" :default DesktopDatePicker]
            ["@mui/lab/AdapterDateFns" :default AdapterDateFns]

            [web.primitives.layout.core :refer [RawGrid]]
            [web.primitives.inputs.utils :as utils]
            [web.primitives.inputs.model :as model]
            [web.primitives.formatting.core :as formatting]
            [web.primitives.inputs.subs :as subs]
            [clojure.string :as string]

            [cljs.pprint :as pprint]))

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

(defn switch-with-two-values [{:keys [id label-placement disabled left-label left-value right-label right-value value]
                               :or   {label-placement "end" disabled false} :as props}]
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
     (js/Error. (str "For switcher with id: \"" id
                     "\" and labels: \"" left-label "\" and \"" right-label
                     "\" expected value is \""
                     left-value "\" or \"" right-value
                     "\" but got \"" (if (nil? value) "nil" value) "\"")))))

(defn RawSwitch
  [{:keys [label label-placement disabled left-label right-label]
    :or   {label-placement "end" disabled false label ""} :as props}]
  (if (and (some? left-label) (some? right-label))
    (switch-with-two-values props)

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
           set-touched set-focused invalid? invalid-date-message
           minDate maxDate min-date max-date]
    :or   {id                   ::date-picker
           required             false
           set-touched          (fn [] (print "set-touched"))
           invalid-date-message "Invalid date"}
    :as   props}]
  (let [component-min-date (or minDate min-date "1900-01-01")
        component-max-date (or maxDate max-date "2099-12-31")]

    [:> LocalizationProvider
     {:dateAdapter AdapterDateFns}
     [:> DesktopDatePicker
      (merge
       props
       {:input-format           "dd.MM.yyyy"
        :allowSameDateSelection true
        :mask                   "__.__.____"
        :label                  label
        :clearable              true
        :clear-text             "Clear"
        :value                  value
        :minDate                (js/Date.parse component-min-date)
        :maxDate                (js/Date.parse component-max-date)
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
                                  (merge
                                   props
                                   {:component-min-date component-min-date
                                    :component-max-date component-max-date
                                    :set-focused        set-focused
                                    :date               %}))})]]))

(declare date-picker-state-id)

(defn date-picker-with-state [{:keys [on-invalid-hook]
                               :or   {on-invalid-hook (fn [])}
                               :as   props}]
  (r/with-let [date-picker-state-id (keyword (str ::date-picker-state- (str (random-uuid))))]
    (let [focused? @(rf/subscribe [::model/date-picker-focused? date-picker-state-id])
          touched? @(rf/subscribe [::model/date-picker-touched? date-picker-state-id])
          date-input-invalid? @(rf/subscribe [::model/date-input-invalid? date-picker-state-id])
          set-focused (fn [focused?] (rf/dispatch [::model/set-date-picker-focused date-picker-state-id focused?]))
          set-touched (fn [] (rf/dispatch [::model/set-date-picker-touched date-picker-state-id]))
          set-date-input-invalid (fn [invalid?]
                                   (doall
                                    (when (not= date-input-invalid? invalid?) (on-invalid-hook invalid?))
                                    (rf/dispatch [::model/set-date-input-invalid date-picker-state-id invalid?])))]
      [RawDatePicker
       (merge
        {:invalid?               (utils/invalid-date? (merge
                                                       {:touched?            touched?
                                                        :focused?            focused?
                                                        :date-input-invalid? date-input-invalid?}
                                                       props))
         :set-focused            set-focused
         :set-touched            set-touched
         :set-date-input-invalid set-date-input-invalid}
        props)])))

(defn EddDatePicker
  [{:keys [id label value read-only? read-only] :as props}]
  (if (or read-only? read-only)
    [RawTextField {:id        id
                   :read-only true
                   :label     label
                   :value     (utils/date-string->ui-view value)}]
    (date-picker-with-state props)))

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

(declare icon-button-state-id)

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

    :on-change       on-change
    :label           label
    :disabled        disabled
    :label-placement label-placement}])

(defn RawRadioGroup
  [{:keys [label children row]
    :or   {row false}
    :as   props}]
  [:> FormControl {}
   (when (and (some? label) (not (string/blank? label)))
     [:> FormLabel {} label])
   (into
    [:> RadioGroup (merge {:row row}
                          (-> props
                              (dissoc :children)
                              (dissoc :label)))]

    (for [item children]
      [:> FormControlLabel
       (merge
        {:control (r/as-element [:> Radio (merge {:color "secondary"})])}
        item)]))])

(defn RawNumberField
  [{:keys [prefix suffix read-only separator default-value]
    :or   {read-only false} :as props}]
  (let [amount-scaling (if read-only
                         @(rf/subscribe [::subs/amount-scaling])
                         "full")
        contains-comma-or-letter? (some? (re-matches #".*[A-Za-z\,].*" (str default-value)))
        formatting-func (get props :formatting-func)]
    [utils/adapted-text-field
     (merge {:full-width true :color "primary" :variant "standard"}
            (dissoc props :formatting-func)
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

            {:on-change (fn [event] (utils/handle-input-change-with-number-formatting event
                                                                                      (get props :on-change
                                                                                           (fn [] (println "BLL")))
                                                                                      formatting-func
                                                                                      separator))}
            {:default-value (if (and read-only contains-comma-or-letter?)
                              default-value
                              (if (some? separator)
                                (pprint/cl-format nil (str "~,,'" separator ":D") (/ default-value 100))
                                (if (some? formatting-func)
                                  (formatting-func default-value)
                                  (formatting/format-number default-value amount-scaling))))})]))

(defn RawPercentField
  [{:keys [prefix suffix read-only default-value]
    :or   {read-only false} :as props}]
  (let [contains-percent-or-letter? (some? (re-matches #".*[A-Za-z\%].*" (str default-value)))]
    [utils/adapted-text-field
     (merge {:full-width true :color "primary" :variant "standard"}
            (dissoc props :transform-func)
            {:InputProps
             (merge
              (when read-only
                {:disableUnderline true :readOnly true})

              (when (some? prefix)
                {:startAdornment (r/as-element
                                  [:> InputAdornment {:position "start"} prefix])})

              {:endAdornment (r/as-element
                              [:> InputAdornment {:position "end"} suffix])})}

            {:on-change (fn [event] (utils/handle-input-change-with-percent-formatting event
                                                                                       (get props :on-change
                                                                                            (fn [] (println "BLL")))))}
            {:default-value (if (and read-only contains-percent-or-letter?)
                              default-value
                              (formatting/format-percent-without-percentage-sign default-value))})]))