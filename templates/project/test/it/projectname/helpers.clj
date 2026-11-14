(ns projectname.helpers
  (:require [testing.pw :as pw]
            [web.primitives.sdk.core :as sdk]))

(def base-url
  (System/getProperty "app.url" "https://projectname.local.localdevhub.com:3000"))

(defmacro with-app
  [& body]
  `(pw/with-ui [page#]
     (sdk/with-page page#
       ~@body)))

(defn open!
  [path ready-el-id]
  (sdk/goto! (str base-url path))
  (sdk/wait-visible (sdk/el ready-el-id)))
