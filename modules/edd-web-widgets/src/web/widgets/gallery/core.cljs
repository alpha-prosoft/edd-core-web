(ns web.widgets.gallery.core
  (:require
   [web.primitives.gallery.core :refer [Gallery]]
   [web.widgets.gallery.catalog :as catalog]))

(defn WidgetsGallery
  "Gallery of the edd widgets. Props are passed on to Gallery."
  [props]
  [Gallery (merge {:register!   catalog/register-all!
                   :title       "Widgets"
                   :intro       "Every widget on one page, wired to its re-frame events."
                   :group-order ["Widgets"]}
                  props)])
