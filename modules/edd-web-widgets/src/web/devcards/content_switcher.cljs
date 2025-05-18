(ns web.devcards.content-switcher
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros (defcard-rg)]
    [reagent.core :as r]
    [web.widgets.content-switcher.views :as views]

    ["@mui/material/Button" :default Button]
    ["@mui/material/Grid" :default Grid]))

(defcard-rg :content-switcher
            "## Content switcher"
            (fn [data-atom]
              [:> Grid {:container true :align-items "center"}
               [:> Grid {:item true :xs 12}
                "Unchangeable content"]
               [:> Grid {:item true :xs 3}
                (views/ContentSwitcher
                  {:checked? (:checked? @data-atom)}
                  [:> Grid {:container true}
                   [:> Button {:on-click #(swap! data-atom merge {:checked? true})}
                    "Delete"]]

                  [:> Grid {:container true :spacing 2}

                    [:> Grid {:item true :xs 12} "Are you sure?"]
                    [:> Grid {:item true}
                     [:> Button {:variant "contained"
                                 :on-click #(swap! data-atom merge {:checked? false})}
                      "Yes"]]
                    [:> Grid {:item true}
                     [:> Button {:variant "outlined"
                                 :on-click #(swap! data-atom merge {:checked? false})}
                      "Cancel"]]])
                ]
               ]
              )
            (r/atom {:checked? false}))
