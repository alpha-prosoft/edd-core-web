(ns web.widgets.lineage.elements
  "Element ids of the timeline, for views and browser tests.")

(defn entry
  [kind]
  (str "lineage-entry-" (name kind)))

(defn message
  "What happened, inside an entry."
  []
  "lineage-message")

(defn details
  "When and who (\"17 Nov 2026, 13:00 · Mia Member\"), inside an entry."
  []
  "lineage-details")

(defn error
  []
  "lineage-error")

(defn fallback
  "A history read from where it was kept before, with its label."
  []
  "lineage-fallback")
