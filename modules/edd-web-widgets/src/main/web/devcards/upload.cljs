(ns web.devcards.upload
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros (defcard-rg)]
    [web.widgets.upload.views :refer [UploadArea ImageWidget]]))

(defcard-rg :upload
            "## Upload"
            (fn []
              [UploadArea {:key "load-area"}]))

(defcard-rg :image-widget
            "## ImageWidget"
            (fn []
              [ImageWidget {:default-value "/missing-image.svg"}]))
