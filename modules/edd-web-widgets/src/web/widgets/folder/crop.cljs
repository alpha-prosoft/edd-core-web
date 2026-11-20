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

(def ^:private scope-class
  "Class on the element wrapping react-image-crop; every rule of css is
   scoped under it so the stylesheet cannot leak onto other UI."
  "folder-crop-scope")

(def ^:private style-id
  "folder-crop-css")

(def ^:private css
  "react-image-crop v11.0.7 stylesheet, every selector scoped under
   scope-class. shadow-cljs does not import CSS through JS, so the dialog
   renders this inline instead, keeping the widget self-contained: the crop
   rectangle lies position:absolute over the position:relative image and the
   handles, mask and marching-ants selection all show, wherever it is used."
  "@keyframes marching-ants{0%{background-position:0 0,0 100%,0 0,100% 0}to{background-position:20px 0,-20px 100%,0 -20px,100% 20px}}
.folder-crop-scope{--rc-drag-handle-size: 12px;--rc-drag-handle-mobile-size: 24px;--rc-drag-handle-bg-colour: rgba(0, 0, 0, .2);--rc-drag-bar-size: 6px;--rc-border-color: rgba(255, 255, 255, .7);--rc-focus-color: #0088ff}
.folder-crop-scope .ReactCrop{position:relative;display:inline-block;cursor:crosshair;max-width:100%}
.folder-crop-scope .ReactCrop *,.folder-crop-scope .ReactCrop *:before,.folder-crop-scope .ReactCrop *:after{box-sizing:border-box}
.folder-crop-scope .ReactCrop--disabled,.folder-crop-scope .ReactCrop--locked{cursor:inherit}
.folder-crop-scope .ReactCrop__child-wrapper{overflow:hidden;max-height:inherit}
.folder-crop-scope .ReactCrop__child-wrapper>img,.folder-crop-scope .ReactCrop__child-wrapper>video{display:block;max-width:100%;max-height:inherit}
.folder-crop-scope .ReactCrop:not(.ReactCrop--disabled) .ReactCrop__child-wrapper>img,.folder-crop-scope .ReactCrop:not(.ReactCrop--disabled) .ReactCrop__child-wrapper>video{touch-action:none}
.folder-crop-scope .ReactCrop:not(.ReactCrop--disabled) .ReactCrop__crop-selection{touch-action:none}
.folder-crop-scope .ReactCrop__crop-mask{position:absolute;top:0;right:0;bottom:0;left:0;pointer-events:none}
.folder-crop-scope .ReactCrop__crop-selection{position:absolute;top:0;left:0;transform:translateZ(0);cursor:move}
.folder-crop-scope .ReactCrop--disabled .ReactCrop__crop-selection{cursor:inherit}
.folder-crop-scope .ReactCrop--circular-crop .ReactCrop__crop-selection{border-radius:50%}
.folder-crop-scope .ReactCrop--circular-crop .ReactCrop__crop-selection:after{pointer-events:none;content:\"\";position:absolute;top:-1px;right:-1px;bottom:-1px;left:-1px;border:1px solid var(--rc-border-color);opacity:.3}
.folder-crop-scope .ReactCrop--no-animate .ReactCrop__crop-selection{outline:1px dashed white}
.folder-crop-scope .ReactCrop__crop-selection:not(.ReactCrop--no-animate .ReactCrop__crop-selection){animation:marching-ants 1s;background-image:linear-gradient(to right,#fff 50%,#444 50%),linear-gradient(to right,#fff 50%,#444 50%),linear-gradient(to bottom,#fff 50%,#444 50%),linear-gradient(to bottom,#fff 50%,#444 50%);background-size:10px 1px,10px 1px,1px 10px,1px 10px;background-position:0 0,0 100%,0 0,100% 0;background-repeat:repeat-x,repeat-x,repeat-y,repeat-y;color:#fff;animation-play-state:running;animation-timing-function:linear;animation-iteration-count:infinite}
.folder-crop-scope .ReactCrop__crop-selection:focus{outline:2px solid var(--rc-focus-color);outline-offset:-1px}
.folder-crop-scope .ReactCrop--invisible-crop .ReactCrop__crop-mask,.folder-crop-scope .ReactCrop--invisible-crop .ReactCrop__crop-selection{display:none}
.folder-crop-scope .ReactCrop__rule-of-thirds-vt:before,.folder-crop-scope .ReactCrop__rule-of-thirds-vt:after,.folder-crop-scope .ReactCrop__rule-of-thirds-hz:before,.folder-crop-scope .ReactCrop__rule-of-thirds-hz:after{content:\"\";display:block;position:absolute;background-color:#fff6}
.folder-crop-scope .ReactCrop__rule-of-thirds-vt:before,.folder-crop-scope .ReactCrop__rule-of-thirds-vt:after{width:1px;height:100%}
.folder-crop-scope .ReactCrop__rule-of-thirds-vt:before{left:33.3333333333%}
.folder-crop-scope .ReactCrop__rule-of-thirds-vt:after{left:66.6666666667%}
.folder-crop-scope .ReactCrop__rule-of-thirds-hz:before,.folder-crop-scope .ReactCrop__rule-of-thirds-hz:after{width:100%;height:1px}
.folder-crop-scope .ReactCrop__rule-of-thirds-hz:before{top:33.3333333333%}
.folder-crop-scope .ReactCrop__rule-of-thirds-hz:after{top:66.6666666667%}
.folder-crop-scope .ReactCrop__drag-handle{position:absolute;width:var(--rc-drag-handle-size);height:var(--rc-drag-handle-size);background-color:var(--rc-drag-handle-bg-colour);border:1px solid var(--rc-border-color)}
.folder-crop-scope .ReactCrop__drag-handle:focus{background:var(--rc-focus-color)}
.folder-crop-scope .ReactCrop .ord-nw{top:0;left:0;transform:translate(-50%,-50%);cursor:nw-resize}
.folder-crop-scope .ReactCrop .ord-n{top:0;left:50%;transform:translate(-50%,-50%);cursor:n-resize}
.folder-crop-scope .ReactCrop .ord-ne{top:0;right:0;transform:translate(50%,-50%);cursor:ne-resize}
.folder-crop-scope .ReactCrop .ord-e{top:50%;right:0;transform:translate(50%,-50%);cursor:e-resize}
.folder-crop-scope .ReactCrop .ord-se{bottom:0;right:0;transform:translate(50%,50%);cursor:se-resize}
.folder-crop-scope .ReactCrop .ord-s{bottom:0;left:50%;transform:translate(-50%,50%);cursor:s-resize}
.folder-crop-scope .ReactCrop .ord-sw{bottom:0;left:0;transform:translate(-50%,50%);cursor:sw-resize}
.folder-crop-scope .ReactCrop .ord-w{top:50%;left:0;transform:translate(-50%,-50%);cursor:w-resize}
.folder-crop-scope .ReactCrop__disabled .ReactCrop__drag-handle{cursor:inherit}
.folder-crop-scope .ReactCrop__drag-bar{position:absolute}
.folder-crop-scope .ReactCrop__drag-bar.ord-n{top:0;left:0;width:100%;height:var(--rc-drag-bar-size);transform:translateY(-50%)}
.folder-crop-scope .ReactCrop__drag-bar.ord-e{right:0;top:0;width:var(--rc-drag-bar-size);height:100%;transform:translate(50%)}
.folder-crop-scope .ReactCrop__drag-bar.ord-s{bottom:0;left:0;width:100%;height:var(--rc-drag-bar-size);transform:translateY(50%)}
.folder-crop-scope .ReactCrop__drag-bar.ord-w{top:0;left:0;width:var(--rc-drag-bar-size);height:100%;transform:translate(-50%)}
.folder-crop-scope .ReactCrop--new-crop .ReactCrop__drag-bar,.folder-crop-scope .ReactCrop--new-crop .ReactCrop__drag-handle,.folder-crop-scope .ReactCrop--fixed-aspect .ReactCrop__drag-bar,.folder-crop-scope .ReactCrop--fixed-aspect .ReactCrop__drag-handle.ord-n,.folder-crop-scope .ReactCrop--fixed-aspect .ReactCrop__drag-handle.ord-e,.folder-crop-scope .ReactCrop--fixed-aspect .ReactCrop__drag-handle.ord-s,.folder-crop-scope .ReactCrop--fixed-aspect .ReactCrop__drag-handle.ord-w{display:none}
@media (pointer: coarse){.folder-crop-scope .ReactCrop .ord-n,.folder-crop-scope .ReactCrop .ord-e,.folder-crop-scope .ReactCrop .ord-s,.folder-crop-scope .ReactCrop .ord-w{display:none}.folder-crop-scope .ReactCrop__drag-handle{width:var(--rc-drag-handle-mobile-size);height:var(--rc-drag-handle-mobile-size)}}")

(defn- ensure-style!
  "Puts css in the document head once, under style-id so it is never
   duplicated, whenever the dialog mounts. shadow-cljs does not import CSS
   through JS, so the widget ships its own stylesheet this way and renders
   correctly wherever it is used, with no host or index.html change."
  []
  (when-not (.getElementById js/document style-id)
    (let [el
          (.createElement js/document "style")]
      (set! (.-id el) style-id)
      (set! (.-textContent el) css)
      (.appendChild (.-head js/document) el))))

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

(defn to-open
  "Of files already screened by web.widgets.folder.files/screen, the pictures
   the cropper should open for: those with no :reason to refuse them. A
   non-picture is thus refused before the dialog opens, not after a crop."
  [screened]
  (mapv :file (remove :reason screened)))

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
        (ensure-style!)
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
             [:div {:class scope-class}
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
                              :display "block"}}]]]]]
           [:> DialogActions
            [:> Button {:on-click on-cancel
                        :data-testid (els/crop-cancel entry)}
             (t :crop-cancel)]
            [:> Button {:variant "contained"
                        :color "primary"
                        :on-click do-confirm
                        :data-testid (els/crop-apply entry)}
             (t :crop-apply)]]]))})))
