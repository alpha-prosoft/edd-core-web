(ns web.widgets.translation.core)

(defn- error-label
  [keys]
  (str "ERR: " (str keys) " doesn't exist in lang map"))

(defn lang-keyword [lang]
  (cond
    (nil? lang) :en
    (keyword? lang) lang
    :else (keyword lang)))

(defn translate
  [lang lang-map k & ks]
  (get-in
   lang-map
   (into [(lang-keyword lang) k] ks)
   (error-label (into [k] ks))))