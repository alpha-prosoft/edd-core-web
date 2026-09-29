(ns web.primitives.sdk.tooltip
  "RawTooltip. el-id lands as data-testid on the wrapping Grid; the tooltip
   popup is portalled with role=tooltip."
  (:require
   [clojure.string :as str]
   [web.primitives.sdk.core :as sdk]))

(defn- popup []
  (.locator sdk/*page* "[role=tooltip]"))

(defn show!
  "Hover the wrapped content and wait for the tooltip."
  [el-id]
  (sdk/hover! el-id)
  (sdk/wait-visible (.first (popup))))

(defn text
  "Hover and return the tooltip text."
  [el-id]
  (show! el-id)
  (str/trim (.innerText (.first (popup)))))

(defn hide!
  "Move the mouse away so the tooltip closes."
  []
  (.move (.mouse sdk/*page*) 0.0 0.0)
  (sdk/wait-hidden (.first (popup))))
