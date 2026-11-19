(ns web.widgets.folder.elements
  "Element ids of the folder widgets, for views and browser tests.")

(defn images
  [entry]
  (str "folder-images-" (name entry)))

(defn image
  "A picture of the list, by its position from 1."
  [entry position]
  (str "folder-image-" (name entry) "-" position))

(defn add
  [entry]
  (str "folder-add-" (name entry)))

(defn input
  [entry]
  (str "folder-input-" (name entry)))

(defn uploading
  [entry]
  (str "folder-uploading-" (name entry)))

(defn problem
  [entry]
  (str "folder-problem-" (name entry)))

(defn status
  "How many pictures of the entry are uploading."
  [entry]
  (str "folder-status-" (name entry)))

(defn refused
  [entry]
  (str "folder-refused-" (name entry)))

(defn remove-image
  [entry position]
  (str "folder-remove-" (name entry) "-" position))

(defn move-earlier
  [entry position]
  (str "folder-earlier-" (name entry) "-" position))

(defn move-later
  [entry position]
  (str "folder-later-" (name entry) "-" position))

(defn crop-dialog
  [entry]
  (str "folder-crop-" (name entry)))

(defn crop-apply
  [entry]
  (str "folder-crop-apply-" (name entry)))

(defn crop-cancel
  [entry]
  (str "folder-crop-cancel-" (name entry)))
