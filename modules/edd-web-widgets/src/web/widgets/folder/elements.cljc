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
