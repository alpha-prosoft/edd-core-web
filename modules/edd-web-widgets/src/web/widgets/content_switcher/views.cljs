(ns web.widgets.content-switcher.views
  (:require
   [web.primitives.components :refer [RawGrid RawZoom]]))

(defn ContentSwitcher [{:keys [checked?]} content checked-content]
  [RawGrid {:container true :sx {:width "100%"}}
   [RawZoom {:in    checked?
             :style (cond-> {:transitionDelay (if checked? "500ms" "0ms")}
                      (false? checked?) (assoc :display "none"))}
    checked-content]
   [RawZoom {:in    (not checked?)
             :style (cond-> {:transitionDelay (if (not checked?) "500ms" "0ms")}
                      checked? (assoc :display "none"))}
    content]])
