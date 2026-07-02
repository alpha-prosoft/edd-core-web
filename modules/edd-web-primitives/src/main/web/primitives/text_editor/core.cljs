(ns web.primitives.text-editor.core
  (:require
   ["react" :refer [useEffect useRef]]
   ["suneditor" :default suneditor :refer [plugins]]
   ["@mui/material/Button" :default Button]
   ["@mui/material/IconButton" :default IconButton]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [dompurify :as dompurify]
   [web.primitives.icons.core :refer [EditIcon]]
   [web.primitives.layout.core :refer [RawGrid]]
   [web.primitives.text-editor.utils :refer [deep-merge sun-editor-default-options normalize-sun-editor-options handle-on-paste handle-on-drop handle-on-save]]
   [web.primitives.text-editor.model :as model]))

(defn sanitize-html [value]
  (dompurify/sanitize value {:USE_PROFILES ["html"]}))

(defn- read-only-mode [props set-edit-mode-funk]
  [RawGrid {:container true
            :size 12
            :sx        {:position "relative"}}
   [RawGrid {:size 12
             :sx                      {:border     "solid 1px #dadada"
                                       :min-height "2rem"
                                       :padding    "1rem"
                                       :display    "block"
                                       :overflow   "hidden"}
             :class-name              "sun-editor-editable"}
    (reagent.core/create-element
     "div"
     #js {:class "se-container se-wrapper"
          :dangerouslySetInnerHTML
          #js {:__html (sanitize-html (:set-contents props))}})]

   (let [edit-icon-position (:edit-icon-position props)]
     [RawGrid {:sx (merge
                    {:position "absolute"}
                    (if (contains? #{:left :right} edit-icon-position)
                      {edit-icon-position 0}
                      {:right 0}))}
      [:> IconButton
       (merge
        {:on-click set-edit-mode-funk
         :sx       (merge
                    {:padding 0}
                    (when (true? (:disable props)) {:opacity 0.5}))
         :disabled (:disable props)}
        (when (:id props) {:id (str "edit-button-" (:id props))}))
       [EditIcon]]])])

(defn- load-image-width-from-file [file]
  (js/Promise.
   (fn [resolve _reject]
     (let [img (js/Image.)
           object-url (js/URL.createObjectURL file)
           cleanup (fn [] (js/URL.revokeObjectURL object-url))]
       (set! (.-onload img)
             (fn []
               (let [width (.-naturalWidth img)]
                 (cleanup)
                 (resolve (when (pos? width) width)))))
       (set! (.-onerror img)
             (fn []
               (cleanup)
               (resolve nil)))
       (set! (.-src img) object-url)))))

(defn- load-image-width-from-url [url]
  (js/Promise.
   (fn [resolve _reject]
     (if (or (nil? url) (= "" url))
       (resolve nil)
       (let [img (js/Image.)]
         (set! (.-onload img)
               (fn []
                 (let [width (.-naturalWidth img)]
                   (resolve (when (pos? width) width)))))
         (set! (.-onerror img) (fn [] (resolve nil)))
         (set! (.-src img) url))))))

(defn- resolve-original-image-width [info]
  (let [files (.-files info)
        url (or (.-url info) (.-src info))]
    (cond
      (and files (some? (.-length files)) (pos? (.-length files)))
      (load-image-width-from-file (aget files 0))

      (and (string? url) (not= "" url))
      (load-image-width-from-url url)

      :else
      (js/Promise.resolve nil))))

(defn- ensure-original-image-width-in-px [params]
  (let [info (.-info params)
        is-update (true? (.-isUpdate info))]
    (if (or (nil? info) is-update)
      (js/Promise.resolve info)
      (.then (resolve-original-image-width info)
             (fn [width]
               (if (number? width)
                 (doto info
                   (aset "inputWidth" (str width "px"))
                   (aset "width" (str width "px"))
                   (aset "sizeUnit" "px"))
                 info))))))

(defn SunEditorNative
  [{:keys [set-contents setOptions on-change on-paste on-drop disable scope-id]}]
  (let [el-ref (useRef nil)
        instance-ref (useRef nil)]

    (useEffect
     (fn []
       (let [filtered-plugins (let [excluded #{"exportPDF" "fileUpload" "layout" "template" "math"}
                                    filtered (js-obj)]
                                (doseq [key (js->clj (js/Reflect.ownKeys plugins))]
                                  (when-not (excluded key)
                                    (aset filtered key (aget plugins key))))
                                filtered)
             callback-events {:onChange (fn [params]
                                          (when on-change
                                            (on-change (.-data params))))
                              :onPaste  (fn [params]
                                          (when on-paste
                                            (on-paste (.-event params) (.-data params))))
                              :onDrop   (fn [params]
                                          (when on-drop
                                            (on-drop (.-event params))))
                              :onImageUploadBefore ensure-original-image-width-in-px}
             merged-options (merge {:plugins filtered-plugins
                                    :value   (or set-contents "")}
                                   setOptions)
             merged-options (update merged-options :events (fn [events]
                                                             (merge (or events {}) callback-events)))
             opts (clj->js (normalize-sun-editor-options merged-options))
             suneditor-api (or (when (.-create suneditor) suneditor)
                               (when (and (.-default suneditor)
                                          (.-create (.-default suneditor)))
                                 (.-default suneditor)))
             _ (when-not suneditor-api
                 (throw (js/Error. "SunEditor API.create unavailable")))
             instance (.create suneditor-api (.-current el-ref) opts)]
         (set! (.-current instance-ref) instance)

         (fn []
           (when-let [editor (.-current instance-ref)]
             (.destroy editor)
             (set! (.-current instance-ref) nil)))))
     #js [set-contents disable scope-id])

    [:textarea {:ref el-ref}]))

(defn- edit-mode [{:keys [on-change editor-scope-id] :as props} set-read-only-mode]
  (let [sun-editor-props (merge
                          {:on-paste     (fn [event clean-data] (handle-on-paste event clean-data props))
                           :on-drop      (fn [] (handle-on-drop props))
                           :scope-id     editor-scope-id
                           :on-change    #(on-change (sanitize-html %))
                           :disable      (:disable props)
                           :set-contents (:set-contents props)
                           :call-plugin  {:image {:float "none"}}}
                          props)]
    [RawGrid (merge
              {:container true
               :size      12
               :id        (str "editor-" editor-scope-id)
               :sx {"img" {:max-width "100% !important"}}})

     [:f> SunEditorNative
      (assoc sun-editor-props :setOptions (deep-merge sun-editor-default-options (:setOptions sun-editor-props)))]
     [RawGrid {:container true
               :size      12
               :sx        {:border          "solid 1px #dadada"
                           :border-top      "none"
                           :justify-content "flex-end"}}
      (when (contains? props :on-save)
        [RawGrid {:sx {:padding "1rem"}}
         [:> Button (merge
                     {:variant  "outlined"
                      :on-click (handle-on-save props set-read-only-mode)
                      :sx       {:height "2rem"}}
                     (when (:id props) {:id (str "save-button-" (:id props))})) "Save"]
         [:> Button (merge
                     {:on-click set-read-only-mode
                      :sx       {:height "2rem"}}
                     (when (:id props) {:id (str "discard-button-" (:id props))})) "Discard"]])]]))

(defn RawTextEditor
  [{:keys [editor-mode set-read-only-mode set-edit-mode disable]
    :or   {editor-mode :edit-mode}
    :as   props}]
  (if
   (or (= :read-only-mode editor-mode)
       disable)
    (read-only-mode props set-edit-mode)
    (edit-mode props set-read-only-mode)))

(declare editor-id)

(defn EddTextEditor
  [props]
  (r/with-let [uuid (str (random-uuid))
               editor-id (keyword (str "rich-text-editor-mode-" uuid))]
    (let [editor-mode @(rf/subscribe [::model/get-editor-mode editor-id])
          editor-scope-id (or (:id props) uuid)
          set-read-only-mode (fn [] (rf/dispatch [::model/set-editor-mode editor-id :read-only-mode]))
          set-edit-mode (fn [] (rf/dispatch [::model/set-editor-mode editor-id :edit-mode]))]
      [RawTextEditor (merge
                      {:editor-mode        editor-mode
                       :editor-scope-id    editor-scope-id
                       :set-read-only-mode set-read-only-mode
                       :set-edit-mode      set-edit-mode}
                      props)])))
