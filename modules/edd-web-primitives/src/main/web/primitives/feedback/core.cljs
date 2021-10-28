(ns web.primitives.feedback.core
  (:require
   ["@mui/material/index" :refer [Alert
                                  Dialog
                                  DialogTitle
                                  DialogContent
                                  DialogActions
                                  LinearProgress
                                  CircularProgress
                                  Snackbar
                                  Backdrop]]
   [reagent.core :as r]))

(defn RawDialog [{:keys [title actions dividers]
                  :or   {dividers false} :as props} content]
  [:> Dialog
   (merge {:disableEnforceFocus true}
          (->
           props
           (dissoc :dividers)
           (dissoc :title)
           (dissoc :actions)))
   [:> DialogTitle title]
   [:> DialogContent {:dividers dividers} content]
   (when (some? actions)
     [:> DialogActions actions])])

(defn RawSnackbar [{:keys [anchor-origin open on-close auto-hide-duration message key alert-props]
                    :as props}
                   content]
  [:> Snackbar (merge
                {:anchor-origin      (merge {:vertical   "bottom"
                                             :horizontal "center"}
                                            anchor-origin)
                 :open               open
                 :on-close           on-close
                 :auto-hide-duration (or auto-hide-duration 5000)
                 :message            (or message "")
                 :key                key}
                (dissoc props :alert-props))
   (when (and (nil? content) (some? alert-props))
     [:> Alert alert-props message])])

(defn RawCircularProgress [props]
  [:> CircularProgress props])

(defn RawBackdrop [props]
  [:> Backdrop props
   [RawCircularProgress {}]])

(defn calculate-progress [time time-left]
  (let [passed-time (if (> time-left time) time (- time time-left))]
    (/ (* passed-time 100) time)))

(declare time-left)
(declare timer)

(defn EddLinearProgress [{:keys [time]}]
  (r/with-let [time-left (r/atom (* 10 time))
               timer (js/setInterval #(swap! time-left dec) 100)]
    (let [progress (calculate-progress (* 10 time) @time-left)]
      (when (= progress 100) (js/clearInterval timer))
      [:> LinearProgress {:variant "determinate"
                          :color   "secondary"
                          :value   progress}])))
