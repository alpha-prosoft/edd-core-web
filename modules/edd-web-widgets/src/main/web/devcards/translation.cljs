(ns web.devcards.translation
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.components :refer [RawGrid]]
   [web.widgets.translation.core :as translation-core]
   [web.widgets.translation.subs :as subs]
   [web.widgets.translation.views :refer [LangSelect]]
   [re-frame.core :as rf]))

(def lang-map
  {:en {:devcards {:language "EN. Language"}}
   :de {:devcards {:language "DE. Sprache"}}
   :ua {:devcards {:language "UA. Мова"}}
   :cr {:devcards {:language "CR. Jezik"}}
   :ru {:devcards {:language "RU. Язык"}}})

(defn tr
  [k & ks]
  (let [lang @(rf/subscribe [::subs/selected-lang])]
    (apply translation-core/translate lang lang-map k ks)))

(defcard-rg :login
  "## LoginBar"
  (fn []
    [RawGrid {:container true :align-items "center"}
     [RawGrid {:item true :xs 1}
      [LangSelect {:id "lang-select" :default-lang :en :lang-list [:en :de :ua :cr :ru]}]]
     [RawGrid {:item true}
      (tr :devcards :language)]]))