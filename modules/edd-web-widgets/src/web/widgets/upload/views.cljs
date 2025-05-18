(ns web.widgets.upload.views
  (:require [reagent.core :as r]
            [clojure.string :as string]

            ["@mui/material/Grid" :default Grid]
            ["@mui/material/Typography" :default Typography]
            ["react-dropzone" :default Dropzone]))

(def default-formats
  {"image/*" [".png", ".gif", ".jpeg", ".jpg"]})

(defn UploadArea
  [{:keys [key
           enabled
           label
           on-file-accepted
           on-file-rejected
           formats
           max-size-mb
           multiple
           grid-styles]
    :or {enabled true
         label "Drop file to upload"
         on-file-accepted (fn [e]
                            (js/console.log "File Accepted" e))
         on-file-rejected (fn [e]
                            (js/console.log "File Rejected" e))
         formats default-formats
         max-size-mb 5
         multiple false
         grid-styles {}}}]
  (let [dropzone-params {:disabled (not enabled)
                         :maxSize (* max-size-mb * 1024 * 1024)
                         :multiple multiple
                         :onDrop (fn [e]
                                   (js/console.log "File Added" e))
                         :onDropAccepted on-file-accepted
                         :onDropRejected on-file-rejected
                         :accept formats}
        formats-label (string/join "," (map
                                        (fn [%]
                                          (string/join ", " %))
                                        (vals formats)))]
    [:> Dropzone dropzone-params
     (fn [props]
       (let [clj-props (js->clj props :keywordize-keys true)
             get-input-props (:getInputProps clj-props)
             get-root-props (:getRootProps clj-props)
             root-props {:id (str key "-container")
                         :ref (.. (get-root-props) -ref)
                         :tabIndex 0
                         :on-key-down (.. (get-root-props) -onKeyDown)
                         :on-focus (.. (get-root-props) -onFocus)
                         :on-blur (.. (get-root-props) -onBlur)
                         :on-click (.. (get-root-props) -onClick)
                         :on-drag-enter (.. (get-root-props) -onDragEnter)
                         :on-drag-over (.. (get-root-props) -onDragOver)
                         :on-drag-leave (.. (get-root-props) -onDragLeave)
                         :on-drop (.. (get-root-props) -onDrop)}
             input-props {:id (str key "-field")
                          :ref (.. (get-input-props) -ref)
                          :accept (.. (get-input-props) -accept)
                          :multiple (.. (get-input-props) -multiple)
                          :style (.. (get-input-props) -style)
                          :on-change (.. (get-input-props) -onChange)
                          :on-click (.. (get-input-props) -onClick)
                          :type (.. (get-input-props) -type)
                          :auto-complete "off"
                          :tab-index -1}]
         (r/as-element
          [:div root-props
           [:> Grid {:container true
                     :sx (merge {:border "2px dashed #ccc"
                                 :border-radius "5px"
                                 :padding "10px"
                                 :background-color "#eee"}
                                grid-styles)
                     :align-items "center"}

            [:> Grid {:item true
                      :xs 1}
             [:svg
              {:focusable "false",
               :aria-hidden "true",
               :viewBox "0 0 24 24",
               :data-testid "AddIcon"}
              [:path {:d "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"}]]]
            [:> Grid {:item true
                      :xs 11
                      :container true
                      :align-items "center"}
             [:> Grid {:item true
                       :xs 12}
              [:> Typography {:variant "body1" :color "textPrimary"} label]]
             [:> Grid {:item true
                       :xs 12}

              [:> Typography {:variant "caption" :color "textSecondary"}
               formats-label]
              [:input input-props]]]]])))]))

(declare uploaded-image)

(defn ImageWidget
  [{:keys [read-only?
           default-value
           on-change]
    :or {default-value
         "/images/missing-image.svg"

         on-change
         (fn [_f])}
    :as _config}]

  (r/with-let [uploaded-image (r/atom nil)]
    [:div {:width   "100%"
           :padding "5px"
           :style {:position "relative"
                   :width "100%"}}
     [:div {:bottom "5px"
            :right "5px"
            :style {:position "absolute"
                    :right "20px"
                    :bottom "20px"
                    :width "90%"
                    :color "white"}}
      (when (not read-only?)
        [UploadArea
         {:label            "Upload event image!"
          :grid-styles   {:opacity "0.9"
                          :width "100%"}
          :on-file-accepted (fn [images]
                              (reset! uploaded-image
                                      (first images))
                              (apply on-change [(first images)]))}])]
     (let [src @uploaded-image]
       [:img {:src    (if src
                        (js/URL.createObjectURL src)
                        default-value)
              :style    {:width   "100%"
                         :padding "5px"}}])]))
