(ns web.widgets.folder.db)

(defn folder-key
  [{:keys [application-id folder]}]
  [(str application-id) (keyword (:kind folder)) (str (:id folder))])

(defn uploads-path
  [props]
  [::uploads (folder-key props)])

(defn upload-path
  [props item]
  (conj (uploads-path props) item))
