(ns web.devcards.list
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawList RawListItem]]))

(defcard-rg :list
  "## List"
  (apply-stiles
   [RawList {}
    [RawListItem {:button true} "Item 1"]
    [RawListItem {:button true} "Item 2"]
    [RawListItem {:button true} "Item 3"]
    [RawListItem {:button true} "Item 4"]
    [RawListItem {:button true} "Item 5"]]))
