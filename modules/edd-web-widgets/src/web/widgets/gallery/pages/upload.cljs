(ns web.widgets.gallery.pages.upload
  (:require
   [web.primitives.gallery.example :refer-macros [example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.upload.views :refer [ImageWidget UploadArea]]))

(def placeholder-image
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='320' height='160'><rect width='100%' height='100%' fill='%23e5e7eb'/></svg>")

(def ^:private props-doc
  [{:name :key              :type "string"  :default "—"     :doc "Prefix for the drop area (-container) and input (-field) ids."}
   {:name :label            :type "string"  :default "\"Drop file to upload\"" :doc "Drop area text."}
   {:name :formats          :type "map"     :default "images" :doc "Accepted MIME types and extensions."}
   {:name :max-size-mb      :type "number"  :default "5"     :doc "Largest accepted file."}
   {:name :multiple         :type "boolean" :default "false" :doc "Allow several files."}
   {:name :enabled          :type "boolean" :default "true"  :doc "Disable to block uploads."}
   {:name :on-file-accepted :type "fn"      :default "logs"  :doc "Called with the accepted files."}
   {:name :on-file-rejected :type "fn"      :default "logs"  :doc "Called with the rejected files."}])

(defn register! []
  (page/register!
   {:id    :upload
    :group "Widgets"
    :title "Upload"
    :doc   "UploadArea is a click-or-drop file area. ImageWidget shows an image with an UploadArea on top that replaces it."
    :props props-doc
    :variants
    [(example {:id :upload-area :title "Upload area"}
              [UploadArea {:key "gallery-upload"}])

     (example {:id :image-widget :title "Image widget"}
              [:div {:data-testid (page/testid :upload :image-widget)
                     :style       {:max-width "320px"}}
               [ImageWidget {:default-value placeholder-image}]])]}))
