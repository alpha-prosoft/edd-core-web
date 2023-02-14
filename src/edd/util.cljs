(ns edd.util
  (:require
   [re-frame.core :as rf]
   [edd.subs :as subs]))

(defn placeholder
  [{:keys [pages classes] :as _ctx}]
  (let [active-panel @(rf/subscribe [::subs/active-panel])]
    (if (contains? pages active-panel)
      (apply
       (get-in pages [active-panel :panel])
       [classes])
      [:h2 "Not found"])))
