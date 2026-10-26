(ns web.widgets.gallery.pages.translation
  (:require
   [re-frame.core :as rf]
   [web.primitives.components :refer [RawGrid]]
   [web.primitives.gallery.example :refer-macros [defdemo example]]
   [web.primitives.gallery.page :as page]
   [web.widgets.translation.core :as translation-core]
   [web.widgets.translation.subs :as subs]
   [web.widgets.translation.views :refer [LangSelect]]))

(def lang-map
  {:en {:gallery {:language "EN. Language"}}
   :de {:gallery {:language "DE. Sprache"}}
   :ua {:gallery {:language "UA. Мова"}}
   :hr {:gallery {:language "HR. Jezik"}}})

(defdemo translated-label []
  (let [lang @(rf/subscribe [::subs/selected-lang])]
    [:span {:data-testid (page/testid :translation :basic :readout)}
     (translation-core/translate lang lang-map :gallery :language)]))

(def ^:private props-doc
  [{:name :default-lang :type "keyword" :default ":en" :doc "Language selected on first render."}
   {:name :lang-list    :type "vector"  :default "—"   :doc "Languages offered in the select."}
   {:name :select-props :type "map"     :default "—"   :doc "Extra props for the RawSelect."}])

(defn register! []
  (page/register!
   {:id    :translation
    :group "Widgets"
    :title "Language select"
    :doc   "LangSelect stores the selected language in re-frame; translate looks a key path up in a language map for it."
    :props props-doc
    :variants
    [(example {:id :basic :title "Switch language"}
              [RawGrid {:container true :spacing 2 :sx {:align-items "center"}}
               [RawGrid {}
                [LangSelect {:select-props {:el-id (page/el :translation :basic)}
                             :default-lang :en
                             :lang-list    [:en :de :ua :hr]}]]
               [RawGrid {}
                [translated-label]]])]}))
