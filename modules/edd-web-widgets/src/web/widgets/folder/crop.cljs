(ns web.widgets.folder.crop
  "Cropping a chosen picture before it is uploaded. Self-contained: a canvas
   and react-image-crop, no other widget dependencies. A folder entry turns it
   on with :crop (true for a free crop, {:aspect ratio} for a fixed one); the
   dialog hands back the cropped region as a js/File of the source picture's
   type, which then goes through the same checks and upload as any other file."
  (:require ["@mui/material" :refer [Box Button Dialog DialogActions DialogContent DialogTitle]]
            ["react-image-crop" :default ReactCrop :refer [centerCrop makeAspectCrop]]
            [reagent.core :as r]
            [web.widgets.folder.elements :as els]))

(def default-texts
  {:crop-title "Crop picture"
   :crop-apply "Apply crop"
   :crop-cancel "Cancel"
   :crop-instructions "Drag to choose the part of the picture to keep."})

(defn spec-crop
  "The :crop of the entry in the folder spec, nil when the entry has none."
  [spec entry]
  (some (fn [e] (when (= entry (:name e)) (:crop e))) spec))

(defn config
  "Whether cropping is on for this entry: the :crop prop of the widget, else
   the :crop of its entry in the folder spec. nil when off."
  [{:keys [crop spec entry]}]
  (or crop (spec-crop spec entry)))

(defn aspect
  "The fixed aspect ratio a crop config asks for, nil for a free crop."
  [crop]
  (when (map? crop) (:aspect crop)))

(defn intercept
  "What to do with the files just chosen: [:crop files] when cropping is on so
   that the dialog opens first, [:upload files] to upload them straight away."
  [crop files]
  [(if crop :crop :upload) (vec files)])

(defn percent-crop->blob
  "Draws the percent-crop region of img-el at natural resolution and returns a
   Promise of a js/File with the source file's name and type (image/png when
   the source has none)."
  [^js img-el ^js pcrop ^js source-file]
  (let [nw
        (.-naturalWidth img-el)

        nh
        (.-naturalHeight img-el)

        sx
        (* (/ (.-x pcrop) 100) nw)

        sy
        (* (/ (.-y pcrop) 100) nh)

        sw
        (* (/ (.-width pcrop) 100) nw)

        sh
        (* (/ (.-height pcrop) 100) nh)

        canvas
        (.createElement js/document "canvas")

        ctx
        (.getContext canvas "2d")

        out-w
        (max 1 (js/Math.round sw))

        out-h
        (max 1 (js/Math.round sh))

        mime
        (or (not-empty (.-type source-file)) "image/png")]
    (set! (.-width canvas) out-w)
    (set! (.-height canvas) out-h)
    (.drawImage ctx img-el sx sy sw sh 0 0 out-w out-h)
    (js/Promise.
     (fn [resolve reject]
       (.toBlob canvas
                (fn [blob]
                  (if blob
                    (resolve (js/File. #js [blob]
                                       (.-name source-file)
                                       #js {:type mime}))
                    (reject (js/Error. "Could not encode the cropped picture"))))
                mime)))))

(defn- initial-crop
  "A centred crop covering most of the loaded picture, keeping the asked
   aspect ratio or the picture's own when none is asked."
  [^js img ratio]
  (let [w
        (.-width img)

        h
        (.-height img)]
    (centerCrop (makeAspectCrop #js {:unit "%" :width 90}
                                (or ratio (/ w h)) w h)
                w h)))

(defn dialog
  "Modal cropper shown before the chosen picture is uploaded. Owns the
   percent-unit crop and the loaded image element; on confirm it makes a
   cropped js/File and passes it to :on-confirm, on cancel or Escape it calls
   :on-cancel. Props: :entry :file :crop :on-confirm :on-cancel :texts."
  [_props]
  (let [pcrop
        (r/atom nil)

        img-ref
        (atom nil)

        src-cache
        (atom nil)]
    (r/create-class
     {:component-will-unmount
      (fn [_]
        (when-let [u (:url @src-cache)]
          (js/URL.revokeObjectURL u)))

      :reagent-render
      (fn [{:keys [entry ^js file crop on-confirm on-cancel texts]}]
        (when (or (nil? @src-cache) (not= (:file @src-cache) file))
          (when-let [old (:url @src-cache)]
            (js/URL.revokeObjectURL old))
          (reset! src-cache {:file file :url (js/URL.createObjectURL file)})
          (reset! pcrop nil))
        (let [t
              (fn [k] (get (merge default-texts texts) k))

              ratio
              (aspect crop)

              title-id
              (str (els/crop-dialog entry) "-title")

              handle-load
              (fn [^js e]
                (reset! pcrop (initial-crop (.-currentTarget e) ratio)))

              do-confirm
              (fn []
                (let [c @pcrop
                      img @img-ref]
                  (if (and c img (pos? (.-width c)) (pos? (.-height c)))
                    (-> (percent-crop->blob img c file)
                        (.then (fn [f] (on-confirm f)))
                        (.catch (fn [_] (on-confirm file))))
                    (on-confirm file))))]
          [:> Dialog {:open true
                      :maxWidth "md"
                      :fullWidth true
                      :on-close on-cancel
                      :aria-labelledby title-id
                      :data-testid (els/crop-dialog entry)}
           [:> DialogTitle {:id title-id} (t :crop-title)]
           [:> DialogContent
            [:> Box {:sx {:display "flex"
                          :justifyContent "center"
                          :alignItems "center"
                          :maxHeight "70vh"
                          :overflow "auto"
                          :bgcolor "action.hover"
                          :borderRadius 1
                          :p 1}}
             [:> ReactCrop {:crop @pcrop
                            :aspect ratio
                            :keepSelection true
                            :aria-label (t :crop-instructions)
                            :onChange (fn [_pixel percent] (reset! pcrop percent))}
              [:img {:src (:url @src-cache)
                     :alt (t :crop-instructions)
                     :ref #(reset! img-ref %)
                     :on-load handle-load
                     :style {:maxWidth "100%"
                             :maxHeight "65vh"
                             :display "block"}}]]]]
           [:> DialogActions
            [:> Button {:on-click on-cancel
                        :data-testid (els/crop-cancel entry)}
             (t :crop-cancel)]
            [:> Button {:variant "contained"
                        :color "primary"
                        :on-click do-confirm
                        :data-testid (els/crop-apply entry)}
             (t :crop-apply)]]]))})))
