(ns web.devcards.upload
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.widgets.upload.views :refer [UploadArea]]))

(defcard-rg :upload
  "## Upload"
  (fn []
    [UploadArea {:key "load-area"}]))
