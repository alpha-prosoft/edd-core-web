(ns web.primitives.feedback.core
  (:require ["@material-ui/core" :refer [Dialog DialogTitle DialogContent
                                         DialogContentText DialogActions
                                         CircularProgress LinearProgress
                                         Snackbar]]
            [reagent.core :as r]))

(defn RawDialog [{:keys [title actions dividers]
                  :or {dividers false} :as props} content]
  [:> Dialog
   (merge {:disableEnforceFocus true}
          (->
           props
           (dissoc :dividers)
           (dissoc :title)
           (dissoc :actions)))
   [:> DialogTitle title]
   [:> DialogContent {:dividers dividers} content]
   (if (some? actions)
     [:> DialogActions actions])])

(defn RawSnackbar [{:keys [anchor-origin open on-close auto-hide-duration message key]}
                   content]
  [:> Snackbar {:anchor-origin      (merge {:vertical   "bottom"
                                            :horizontal "center"}
                                           anchor-origin)
                :open               open
                :on-close           on-close
                :auto-hide-duration (or auto-hide-duration 5000)
                :message            (or message "")
                :key                key}
   content])

(defn RawCircularProgress [props]
  [:> CircularProgress props])

(defn calculate-progress [time time-left]
  (let [passed-time (if (> time-left time) time (- time time-left))]
    (/ (* passed-time 100) time)))

(defn EddLinearProgress [{:keys [time]}]
  (r/with-let [time-left (r/atom time)
               timer (js/setInterval #(swap! time-left dec) 1000)]
    (let [progress (calculate-progress time @time-left)]
      (when (= progress 100) (js/clearInterval timer))
      [:> LinearProgress {:variant "determinate"
                          :color "secondary"
                          :value progress}])))
