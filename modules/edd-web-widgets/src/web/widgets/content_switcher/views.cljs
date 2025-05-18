(ns web.widgets.content-switcher.views
  (:require
    ["@mui/material/Grid" :default Grid]
    ["@mui/material/Zoom" :default Zoom]))

(defn ContentSwitcher [{:keys [checked?]} content checked-content]
  [:> Grid {:container true}
   [:> Zoom {:in checked?
              :style (cond-> {:transitionDelay (if checked? "500ms" "0ms")}
                             (false? checked?) (assoc :display "none"))}
     checked-content]
   [:> Zoom {:in (not checked?)
             :style (cond-> {:transitionDelay (if (not checked?) "500ms" "0ms")}
                            checked? (assoc :display "none"))}
    content]])