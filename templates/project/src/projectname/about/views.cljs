(ns projectname.about.views
  (:require [re-frame.core :as rf]
            [projectname.about.subs :as subs]
            [projectname.about.events :as events]

            ["@mui/material/Grid" :default Grid]))

(defn main-panel
  [classes]
  [:> Grid {:container true
            :item      true}
   [:> Grid {:item true
             :xs   12}
    (:id @(rf/subscribe [::subs/params]))]])







