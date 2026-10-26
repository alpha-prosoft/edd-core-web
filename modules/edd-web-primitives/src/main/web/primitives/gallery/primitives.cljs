(ns web.primitives.gallery.primitives
  (:require
   [web.primitives.gallery.catalog :as catalog]
   [web.primitives.gallery.core :refer [Gallery]]))

(defn PrimitivesGallery
  "Gallery of every built-in web primitive. Props are passed on to Gallery."
  [props]
  [Gallery (merge {:register! catalog/register-all!
                   :title     "Primitives"
                   :intro     "Every primitive on one page. Use the sidebar to jump to a component; each example's El-ids button shows the values the SDK scan reads."}
                  props)])
