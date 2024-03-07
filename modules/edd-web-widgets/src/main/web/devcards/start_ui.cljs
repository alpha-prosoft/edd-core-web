(ns web.devcards.start-ui
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :as dc]
   [marked :as marked]
   [web.devcards.login]
   [web.devcards.translation]
   [web.devcards.upload]
   [web.devcards.snackbar-alert]
   [web.devcards.confirmation-dialog]
   [web.devcards.content-switcher]))

(js/goog.exportSymbol "DevcardsMarked" marked/marked)

(defn ^:export init []
  (dc/start-devcard-ui!))
