(ns web.widgets.translation.core)

(defn- error-label
  [keys]
  (str "ERR: " (str keys) " doesn't exist in lang map"))

(defn translate
  [lang lang-map & ks]
  (let [lang (or lang
                 :en)
        lang (keyword lang)
        path (into [lang] ks)]
    (get-in
     lang-map
     path
     (error-label (.stringify js/JSON
                              (clj->js
                               path))))))
