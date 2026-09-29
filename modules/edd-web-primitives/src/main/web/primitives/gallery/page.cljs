(ns web.primitives.gallery.page
  (:require
   [web.primitives.gallery.registry :as registry]
   [web.primitives.testing.elements :as els]))

(defn el
  "El-id data for a demo element; pass it to a primitive's :el-id."
  ([page variant]
   (els/el page variant))
  ([page variant role]
   (els/el page variant role)))

(defn testid
  "Resolved id string for a raw :data-testid on a plain (non-primitive) element."
  [& parts]
  (apply els/testid parts))

(defn register!
  "Register a component page.

   (register! {:id       :button
               :group    \"Inputs\"
               :title    \"Button\"
               :doc      \"...\"
               :props    [{:name :variant :type \"string\" :default \"text\" :doc \"...\"}]
               :variants [(example {:id :contained :title \"Contained\"} [RawButton ...])]})

   Build each variant with web.primitives.gallery.example/example so the shown code
   and the rendered demo come from the same form."
  [page-def]
  (registry/register-page! page-def))
