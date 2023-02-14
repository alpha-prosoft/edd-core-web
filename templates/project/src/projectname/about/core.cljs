(ns projectname.about.core
  (:require [projectname.about.events :as events]
            [projectname.about.views :as views]))


(defn main
  [_ctx]
  {:init ::events/init
   :panel views/main-panel})
