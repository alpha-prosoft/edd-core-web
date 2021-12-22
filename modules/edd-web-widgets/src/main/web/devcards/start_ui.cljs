(ns web.devcards.start-ui
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :as dc]
   [web.devcards.login]
   [web.devcards.translation]))

(defn ^:export init []
  (dc/start-devcard-ui!))