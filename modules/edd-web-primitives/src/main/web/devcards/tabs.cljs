(ns web.devcards.tabs
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawGrid RawTabs]]))

(defcard-rg :tabs "## Tabs"
  (fn [data-atom _]
    (apply-stiles [RawGrid {:container true}
                   [RawGrid {:item true :xs 12}
                    [RawTabs {:value (:selected @data-atom)
                              :on-change #(swap! data-atom merge {:selected (-> %2)})
                              :indicatorColor "primary"
                              :tabs [{:id (str "Tab Number 1")
                                      :label "Tab 1"}
                                     {:id (str "Tab Number 2")
                                      :label "Tab 2"}
                                     {:id (str "Tab Number 3")
                                      :label "Disabled Tab" :disabled true}]}]]
                   [RawGrid {:item true :xs 12}
                    (case (:selected @data-atom)
                      0 [RawGrid {:item true :xs 6} "Content of Tab 1"]
                      1 [RawGrid {:item true :xs 6} "Content of Tab 2"]
                      2 [RawGrid {:item true :xs 6} "Content of Tab 3"]
                      "")]]))
  (r/atom {:selected 0}))