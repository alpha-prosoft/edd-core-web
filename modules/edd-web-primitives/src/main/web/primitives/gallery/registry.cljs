(ns web.primitives.gallery.registry
  "Registry of gallery pages. A page is registered once (usually at namespace
   load time) and rendered later by EddComponentGallery."
  (:require
   [reagent.core :as r]))

(defonce pages (r/atom (sorted-map)))

(defn register-page!
  [{:keys [id] :as page-def}]
  (swap! pages assoc id page-def))

(defn all-pages []
  (vals @pages))

(defn page-by-id [id]
  (get @pages id))
