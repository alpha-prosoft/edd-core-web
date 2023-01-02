(ns web.devcards.chip
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawStack RawChip]]))

(defcard-rg :chip
  "## Chip"
  (apply-stiles
   [RawStack {:direction "row" :spacing 1}
    [RawChip {:label "One"}]
    [RawChip {:label "Two"}]]))

(defcard-rg :chip-clickable
  "## Chip Clickable"
  (fn [data-atom _]
    (let [{:keys [selected items]} @data-atom]
      (apply-stiles
       (into
        [RawStack {:direction "row" :spacing 1}]
        (for [item items]
          [RawChip {:label     item
                    :clickable true
                    :color     "secondary"
                    :size      "small"
                    :on-click #(swap! data-atom merge {:selected item})
                    :sx        (when (not= selected item)
                                 {:opacity 0.5})}])))))
  (r/atom {:selected :one :items [:one :two :tree]}))

