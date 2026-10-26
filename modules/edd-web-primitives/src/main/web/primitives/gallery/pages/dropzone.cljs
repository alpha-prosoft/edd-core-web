(ns web.primitives.gallery.pages.dropzone
  (:require
   [reagent.core :as r]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.primitives.inputs.core :refer [RawDropzone]]))

(defdemo drop-area []
  (let [files (r/atom [])]
    (fn []
      [:div
       [RawDropzone {:el-id            (page/el :dropzone :basic)
                     :multiple         true
                     :on-drop-accepted #(reset! files (mapv (fn [^js f] (.-name f)) %))}
        [:div {:style {:padding "24px" :border "2px dashed #ccc" :border-radius "6px" :background "#fafafa"}}
         "Drop files here or click to choose"]]
       [:div {:data-testid (page/testid :dropzone :basic :readout)}
        (str "files:" (count @files))]])))

(def ^:private props-doc
  [{:name :accept           :type "map"     :default "—"     :doc "Accepted MIME types, e.g. {\"image/*\" [\".png\"]}."}
   {:name :multiple         :type "boolean" :default "true"  :doc "Allow several files."}
   {:name :max-size         :type "bytes"   :default "—"     :doc "Largest accepted file."}
   {:name :disabled         :type "boolean" :default "false" :doc "Disables dropping and the file dialog."}
   {:name :on-drop-accepted :type "fn"      :default "—"     :doc "Called with the accepted File array."}
   {:name :on-drop-rejected :type "fn"      :default "—"     :doc "Called with the rejected files."}
   {:name :root-props       :type "map"     :default "—"     :doc "Extra props for the drop area div."}
   {:name :input-props      :type "map"     :default "—"     :doc "Extra props for the hidden file input."}
   {:name :el-id            :type "keyword | string" :default "—" :doc "Lands as data-testid on the drop area."}])

(defn register! []
  (page/register!
   {:id    :dropzone
    :group "Inputs"
    :title "Dropzone"
    :doc   "File drop area backed by react-dropzone. Children render inside the clickable drop area; other props go to react-dropzone."
    :props props-doc
    :variants
    [(example {:id :basic :title "Basic"}
              [drop-area])]}))
