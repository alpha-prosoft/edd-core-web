(ns web.primitives.gallery.pages.feedback
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.feedback.core :refer [RawDialog RawSnackbar RawBackdrop RawCircularProgress]]
   [web.primitives.inputs.core :refer [RawButton]]))

(defdemo dialog []
  (let [state (r/atom {:open false})]
    (fn []
      [:div
       [RawButton {:el-id    (page/el :feedback :dialog-open)
                   :variant  "contained"
                   :on-click #(swap! state assoc :open true)}
        "Open dialog"]
       [RawDialog {:el-id   (page/el :feedback :dialog)
                   :open    (:open @state)
                   :onClose #(swap! state assoc :open false)
                   :title   "Title"
                   :actions (r/as-element
                             [RawButton {:el-id    (page/el :feedback :dialog-close)
                                         :on-click #(swap! state assoc :open false)}
                              "Close"])}
        "Dialog body"]])))

(defdemo snackbar []
  (let [state (r/atom {:open false})]
    (fn []
      [:div
       [RawButton {:el-id    (page/el :feedback :snackbar-open)
                   :variant  "contained"
                   :on-click #(swap! state assoc :open true)}
        "Show snackbar"]
       [RawSnackbar {:el-id    (page/el :feedback :snackbar)
                     :open     (:open @state)
                     :message  "Saved"
                     :on-close #(swap! state assoc :open false)}
        nil]])))

(defdemo backdrop []
  (let [state (r/atom {:open false})]
    (fn []
      [:div
       [RawButton {:el-id    (page/el :feedback :backdrop-open)
                   :variant  "contained"
                   :on-click #(swap! state assoc :open true)}
        "Show backdrop"]
       [RawBackdrop {:el-id    (page/el :feedback :backdrop)
                     :open     (:open @state)
                     :on-click #(swap! state assoc :open false)}]])))

(def ^:private props-doc
  [{:name :open               :type "boolean" :default "false" :doc "Dialog / Snackbar / Backdrop: visibility."}
   {:name :title              :type "node"    :default "—"     :doc "Dialog: title."}
   {:name :actions            :type "element" :default "nil"   :doc "Dialog: footer actions (use r/as-element)."}
   {:name :dividers           :type "boolean" :default "false" :doc "Dialog: dividers around content."}
   {:name :onClose            :type "fn"      :default "—"     :doc "Dialog: close handler."}
   {:name :message            :type "node"    :default "\"\""  :doc "Snackbar: message."}
   {:name :auto-hide-duration :type "ms"      :default "5000"  :doc "Snackbar: auto close delay."}
   {:name :anchor-origin      :type "map"     :default "bottom center" :doc "Snackbar: position."}
   {:name :alert-props        :type "map"     :default "nil"   :doc "Snackbar: render an Alert with these props."}
   {:name :el-id              :type "keyword | string" :default "—" :doc "Lands as data-testid."}])

(defn register! []
  (page/register!
   {:id    :feedback
    :group "Feedback"
    :title "Feedback (dialog / snackbar / backdrop / progress)"
    :doc   "Overlays and progress indicators. Legacy PaperProps / TransitionProps are moved into slotProps automatically."
    :props props-doc
    :variants
    [(example {:id :dialog :title "Dialog"}
              [dialog])

     (example {:id :snackbar :title "Snackbar"}
              [snackbar])

     (example {:id :backdrop :title "Backdrop"}
              [backdrop])

     (example {:id :progress :title "Circular progress"}
              [RawCircularProgress {:el-id (page/el :feedback :progress)}])]}))
