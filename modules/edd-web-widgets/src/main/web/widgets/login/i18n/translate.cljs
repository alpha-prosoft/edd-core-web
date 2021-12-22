(ns web.widgets.login.i18n.translate
  (:require
   [goog.object :as g]
   [re-frame.core :as rf]
   [web.widgets.login.i18n.en :as en]
   [web.widgets.login.i18n.de :as de]
   [web.widgets.login.i18n.ua :as ua]
   [web.widgets.login.i18n.ru :as ru]
   [web.widgets.translation.subs :as subs]
   [web.widgets.translation.core :as translation-core]))

(def lang-map
  (let [additional-lang-map (or (g/get js/window "translation.widgets.login") {})]
    (merge
     en/tr-en
     de/tr-de
     ua/tr-ua
     ru/tr-ru
     additional-lang-map)))

(defn tr
  [k & ks]
  (let [lang @(rf/subscribe [::subs/selected-lang])]
    (apply translation-core/translate lang lang-map k ks)))
