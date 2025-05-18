(ns web.widgets.upload.core
  (:require [web.widgets.upload.events :as events]
            [web.widgets.upload.views :as views]))

(defn main
  [ctx]
  {:init ::events/init
   :panel views/main-panel})
