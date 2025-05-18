(ns web.devcards.login
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.widgets.login.views :refer [LoginBar]]))

(defcard-rg :login
  "## LoginBar"
  (fn []
    [LoginBar {:id "login-bar"}]))