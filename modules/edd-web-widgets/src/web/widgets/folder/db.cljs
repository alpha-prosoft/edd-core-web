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

(defn waiting-path
  [props]
  [::waiting (folder-key props)])

(defn files-path
  "The files of the last :folder->get, for resolving the signed url of a
   private entry."
  [props]
  [::files (folder-key props)])

(defn watch-path
  [props entry]
  [::watch (folder-key props) entry])

(def order-path
  [::order])
