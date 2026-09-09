(ns web.primitives.text-editor.core
  (:require
   ["react" :refer [useEffect useRef]]
   ["suneditor" :default suneditor :refer [plugins]]
   ["@mui/material/Button" :default Button]
   ["@mui/material/IconButton" :default IconButton]
   [clojure.string :as str]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [dompurify :as dompurify]
   [web.primitives.icons.core :refer [EditIcon]]
   [web.primitives.layout.core :refer [RawGrid]]
   [web.primitives.text-editor.utils :refer [copied-cell-payload deep-merge handle-on-drop handle-on-paste handle-on-save maybe-normalize-paste-artifacts! normalize-sun-editor-options resolve-selected-single-cell sun-editor-default-options sync-li-style-marker]]
   [web.primitives.text-editor.model :as model]))

(defn sanitize-html [value]
  (dompurify/sanitize value #js {:USE_PROFILES #js {:html true}
                                 :ADD_ATTR     #js ["style" "target" "data-se-li-style"]
                                 ;; Keep temporary/local image URLs visible right after save.
                                 :ALLOWED_URI_REGEXP #"^(?:(?:https?|mailto|tel|ftp|file|blob|data):|[^a-z]|[a-z+.-]+(?:[^a-z+.-:]|$))"}))

(defn- editable-element-id [scope-id]
  (when scope-id
    (str "text-editor-editable-" scope-id)))

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
          #js {:__html (sanitize-html (sync-li-style-marker (:set-contents props)))}})]

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

(def default-editor-wrapper-style "font-family: Amalia; font-size: 14px; width: 100%;")
(def default-table-style "font-size: 14px; max-width: 100%;")

(def default-value "<p>\u200B</p>")

(defn- blank-text-node? [node]
  (and (= (.-nodeType node) js/Node.TEXT_NODE)
       (str/blank? (or (.-nodeValue node) ""))))

(defn- default-wrapper-root? [node]
  (let [style (some-> (.getAttribute node "style") str/lower-case)]
    (and node
         (= (.-nodeType node) js/Node.ELEMENT_NODE)
         (= "div" (some-> (.-tagName node) str/lower-case))
         (string? style)
         (str/includes? style "font-family: amalia")
         (str/includes? style "font-size: 14px"))))

(defn- unwrap-default-editor-style [value]
  (let [content (or value "")
        container (.createElement js/document "div")]
    (set! (.-innerHTML container) content)
    (let [nodes (->> (array-seq (.-childNodes container))
                     (remove blank-text-node?))
          only-node (first nodes)]
      (if (and (= 1 (count nodes))
               (default-wrapper-root? only-node))
        (.-innerHTML only-node)
        content))))

(defn- apply-default-table-style! [container]
  (doseq [table (array-seq (.querySelectorAll container "table"))]
    (let [current-style (or (.getAttribute table "style") "")
          normalized-style (str/lower-case current-style)
          with-font-size (if (str/includes? normalized-style "font-size:")
                           current-style
                           (str current-style (when-not (str/blank? current-style) " ") "font-size: 14px;"))
          with-default-style (if (str/includes? (str/lower-case with-font-size) "max-width:")
                               with-font-size
                               (str with-font-size " " default-table-style))]
      (.setAttribute table "style" (str/trim with-default-style)))))

(defn- wrap-with-default-editor-style [value]
  (let [content (or value "")
        container (.createElement js/document "div")]
    (set! (.-innerHTML container) content)
    (apply-default-table-style! container)
    (let [normalized-content (.-innerHTML container)
          nodes (->> (array-seq (.-childNodes container))
                     (remove blank-text-node?))
          only-node (first nodes)
          already-wrapped? (and (= 1 (count nodes))
                                (default-wrapper-root? only-node))]
      (if already-wrapped?
        normalized-content
        (str "<div style=\"" default-editor-wrapper-style "\">" normalized-content "</div>")))))

(defn- extract-font-size-value [font-size]
  (when (string? font-size)
    (let [trimmed (str/trim font-size)]
      (or (second (re-matches #"^(\d+(?:\.\d+)?)px$" trimmed))
          trimmed))))

(defn- sync-font-size-label! [scope-id]
  (when scope-id
    (let [container (.getElementById js/document (str "editor-" scope-id))
          editable (when container (.querySelector container ".sun-editor-editable"))
          label-el (when container (.querySelector container ".se-btn-tool-font-size .se-txt"))
          selection (.getSelection js/window)
          node (when selection (.-anchorNode selection))
          element (cond
                    (nil? node) nil
                    (= (.-nodeType node) js/Node.TEXT_NODE) (.-parentElement node)
                    :else node)
          active-el (if (and editable element (.contains editable element))
                      element
                      editable)
          font-size (when active-el (.-fontSize (.getComputedStyle js/window active-el)))
          value (extract-font-size-value font-size)]
      (when (and label-el value)
        (set! (.-textContent label-el) value)))))

(defn- maybe-copy-single-table-cell! [event editable]
  (let [selection (.getSelection js/window)
        has-range? (and selection (pos? (.-rangeCount selection)))
        has-text-selection? (and has-range? (not (.-isCollapsed selection)))
        single-cell (resolve-selected-single-cell editable selection)]
    (when (and single-cell (not has-text-selection?))
      (let [clipboard-data (.-clipboardData event)
            {:keys [text html]} (copied-cell-payload single-cell)]
        (when clipboard-data
          (.setData clipboard-data "text/plain" text)
          (.setData clipboard-data "text/html" html)
          (.preventDefault event))))))

(defn- copy-single-table-cell-manually! [editable]
  (let [selection (.getSelection js/window)
        single-cell (resolve-selected-single-cell editable selection)]
    (when single-cell
      (let [{:keys [text html]} (copied-cell-payload single-cell)
            navigator-clipboard (some-> js/navigator .-clipboard)
            can-write-items? (and navigator-clipboard
                                  (exists? js/ClipboardItem)
                                  (exists? js/Blob))]
        (cond
          can-write-items?
          (.write navigator-clipboard
                  #js [(js/ClipboardItem.
                        #js {"text/plain" (js/Blob. #js [text] #js {:type "text/plain"})
                             "text/html"  (js/Blob. #js [html] #js {:type "text/html"})})])

          navigator-clipboard
          (.writeText navigator-clipboard text)

          :else
          (js/Promise.reject (js/Error. "Clipboard API unavailable")))))))

(defn- maybe-handle-copy-button-click! [event editable]
  (let [target (.-target event)
        copy-button (when target
                      (.closest target "button.se-btn-tool-copy,button[data-command='copy']"))
        selection (.getSelection js/window)
        single-cell (resolve-selected-single-cell editable selection)]
    (when (and copy-button single-cell)
      ;; Avoid SunEditor's built-in failed-copy message for the single-cell case.
      (.preventDefault event)
      (.stopPropagation event)
      (.then (copy-single-table-cell-manually! editable)
             (fn [_] true)
             (fn [_]
               ;; Keep focus stable and let user still use keyboard copy if browser blocks clipboard write.
               (.focus editable))))))

(defn SunEditorNative
  [{:keys [set-contents setOptions on-change on-paste on-drop disable scope-id]}]
  (let [set-contents (-> (if (str/blank? set-contents)
                           default-value
                           set-contents)
                         (unwrap-default-editor-style)
                         (sync-li-style-marker))
        el-ref (useRef nil)
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
                                          (let [normalized-data (-> (.-data params)
                                                                    (sync-li-style-marker))]
                                            (when on-change
                                              (on-change normalized-data))
                                            (js/setTimeout #(sync-font-size-label! scope-id) 0)))

                              :onPaste  (fn [params]
                                          (when on-paste
                                            (on-paste (.-event params) (.-data params)))
                                          (js/setTimeout #(sync-font-size-label! scope-id) 0))
                              :onDrop   (fn [params]
                                          (when on-drop
                                            (on-drop (.-event params)))
                                          (js/setTimeout #(sync-font-size-label! scope-id) 0))
                              :onImageUploadBefore ensure-original-image-width-in-px}
             merged-options (merge {:plugins filtered-plugins
                                    :value   set-contents}
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
             instance (.create suneditor-api (.-current el-ref) opts)
             schedule-sync (fn [] (js/setTimeout #(sync-font-size-label! scope-id) 0))
             container (when scope-id (.getElementById js/document (str "editor-" scope-id)))
             editable (when container (.querySelector container ".sun-editor-editable"))
             listener (fn [] (schedule-sync))
             copy-listener (fn [event] (maybe-copy-single-table-cell! event editable))
             click-listener (fn [event] (maybe-handle-copy-button-click! event editable))
             paste-listener (fn [event] (maybe-normalize-paste-artifacts! event editable))]
         (set! (.-current instance-ref) instance)
         (schedule-sync)

         (when editable
           (set! (.-id editable) (editable-element-id scope-id))
           (.addEventListener editable "keyup" listener)
           (.addEventListener editable "mouseup" listener)
           (.addEventListener editable "input" listener)
           (.addEventListener editable "copy" copy-listener)
           (.addEventListener editable "paste" paste-listener true))

         (when container
           ;; Toolbar buttons are outside editable area, so listen on the editor container.
           (.addEventListener container "click" click-listener true)
           (.addEventListener container "paste" paste-listener true))

         (fn []
           (when editable
             (.removeEventListener editable "keyup" listener)
             (.removeEventListener editable "mouseup" listener)
             (.removeEventListener editable "input" listener)
             (.removeEventListener editable "copy" copy-listener)
             (.removeEventListener editable "paste" paste-listener true))
           (when container
             (.removeEventListener container "click" click-listener true)
             (.removeEventListener container "paste" paste-listener true))
           (when-let [editor (.-current instance-ref)]
             (.destroy editor)
             (set! (.-current instance-ref) nil)))))
     #js [set-contents disable scope-id])

    [:textarea {:ref el-ref}]))

(defn- edit-mode [{:keys [on-change editor-scope-id] :as props} set-read-only-mode]
  (let [sun-editor-props (-> props
                             (assoc :on-paste (fn [event clean-data] (handle-on-paste event clean-data props))
                                    :on-drop (fn [] (handle-on-drop props))
                                    :scope-id editor-scope-id
                                    :on-change #(on-change (sanitize-html (wrap-with-default-editor-style (sync-li-style-marker %))))
                                    :disable (:disable props)
                                    :set-contents (:set-contents props)
                                    :call-plugin {:image {:float "none"}}))]
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
