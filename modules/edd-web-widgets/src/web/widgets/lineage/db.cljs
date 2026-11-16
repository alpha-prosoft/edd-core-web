(ns web.widgets.lineage.db)

(defn stream-key
  [{:keys [scope slice]}]
  [(keyword scope) (mapv str slice)])

(defn stream-path
  [ref]
  [::streams (stream-key ref)])

(defn user-path
  [user-id]
  [::users (str user-id)])
