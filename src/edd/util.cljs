(ns edd.util
  (:require
   [re-frame.core :as rf]
   [edd.subs :as subs]))

(defn placeholder
  [{:keys [panels classes] :as _ctx}]
  (let [panel @(rf/subscribe [::subs/active-panel])]
    (if (contains? panels panel)
      ((panel panels) classes)
      [:h2 "Not found"])))
