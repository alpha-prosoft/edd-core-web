(ns web.primitives.testing.elements
  "El-id data for gallery demos. Returns plain :el-id values (vectors), so a
   view passes them straight to a primitive's :el-id and a test passes the
   same value to the SDK; neither builds an id string.

     (el :autocomplete :basic)          -> [:autocomplete :basic]           ; \"autocomplete--basic\"
     (el :slider :basic :readout)       -> [:slider :basic :readout]        ; \"slider--basic--readout\"
     (testid :slider :basic :readout)   -> \"slider--basic--readout\"         ; for raw :data-testid"
  (:require
   [web.primitives.testing.el-id :refer [el-id]]))

(defn el
  "El-id for a gallery demo: `page` and `variant`, plus an optional sub-element
   `role` such as :readout."
  ([page variant]
   [page variant])
  ([page variant role]
   [page variant role]))

(defn testid
  "Resolved id string for raw DOM attributes (:data-testid on plain elements)."
  [& parts]
  (el-id (apply el parts)))
