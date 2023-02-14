(ns projectname.home.core
  (:require [projectname.home.events :as events]
            [projectname.home.views :as views]))

(defn main
  [ctx]
  {:init ::events/init
   :panel views/main-panel})
