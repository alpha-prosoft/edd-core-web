(ns web.primitives.feedback.core
  (:require
   [reagent.core :as r]
   [web.primitives.utils :as utils]

   ["@mui/material/Alert" :default Alert]
   ["@mui/material/Dialog" :default Dialog]
   ["@mui/material/DialogTitle" :default DialogTitle]
   ["@mui/material/DialogContent" :default DialogContent]
   ["@mui/material/DialogActions" :default DialogActions]
   ["@mui/material/LinearProgress" :default LinearProgress]
   ["@mui/material/CircularProgress" :default CircularProgress]
   ["@mui/material/Snackbar" :default Snackbar]
   ["@mui/material/Backdrop" :default Backdrop]))

(defn RawDialog [{:keys [title title-props actions dividers]
                  :or   {dividers false title-props {}} :as props} content]
  [:> Dialog
   (->  props
        (dissoc :dividers :title :title-props :actions)
        (utils/with-el-id-as-testid)
        (utils/handle-paper-props-migration)
        (utils/handle-transition-props-migration))
   [:> DialogTitle title-props title]
   (when (some? content)
     [:> DialogContent {:dividers dividers} content])
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
                (utils/with-el-id-as-testid (dissoc props :alert-props)))
   (when (and (nil? content) (some? alert-props))
     [:> Alert alert-props message])])

(defn RawCircularProgress [props]
  [:> CircularProgress (utils/with-el-id-as-testid props)])

(defn RawBackdrop [props]
  [:> Backdrop (utils/with-el-id-as-testid props)
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
