(ns web.widgets.upload.views
  (:require [reagent.core :as r]
            [clojure.string :as string]
            [web.primitives.components :refer [RawDropzone RawGrid RawTypography]]
            [web.primitives.icons.core :refer [AddIcon]]))

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
  (let [formats-label (string/join "," (map
                                        (fn [%]
                                          (string/join ", " %))
                                        (vals formats)))]
    [RawDropzone {:disabled       (not enabled)
                  :maxSize        (* max-size-mb 1024 1024)
                  :multiple       multiple
                  :onDrop         (fn [e]
                                    (js/console.log "File Added" e))
                  :onDropAccepted on-file-accepted
                  :onDropRejected on-file-rejected
                  :accept         formats
                  :root-props     {:id (str key "-container")}
                  :input-props    {:id (str key "-field")}}
     [RawGrid {:container true
               :sx (merge {:border "2px dashed #ccc"
                           :border-radius "5px"
                           :padding "10px"
                           :background-color "#eee"
                           :align-items "center"
                           :width "100%"}
                          grid-styles)}
      [RawGrid {:size 1}
       [AddIcon {}]]
      [RawGrid {:container true
                :size 11
                :sx {:align-items "center"}}
       [RawGrid {:size 12}
        [RawTypography {:variant "body1" :color "text.primary"} label]]
       [RawGrid {:size 12}
        [RawTypography {:variant "caption" :color "text.secondary"}
         formats-label]]]]]))

(declare uploaded-image)

(defn ImageWidget
  [{:keys [read-only?
           default-value
           label
           on-change]
    :or {default-value
         "/images/missing-image.svg"

         label
         "Upload image"

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
         {:label            label
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
