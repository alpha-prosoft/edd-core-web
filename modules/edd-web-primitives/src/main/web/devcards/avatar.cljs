(ns web.devcards.avatar
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawAvatar]]
   [web.primitives.icons.core :refer [AccountBalanceWalletIcon]]))

(defcard-rg :string-avatar
  "## String Avatar"
  (apply-stiles [RawAvatar {:sx {:bgcolor "red"}} "JS"]))

(defcard-rg :string-avatar
  "## String Avatar"
  (apply-stiles [RawAvatar {:sx {:bgcolor "blue"}} [AccountBalanceWalletIcon {}]]))