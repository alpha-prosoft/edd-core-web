(ns web.tree-view
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawTreeView RawTreeItem]]))

(defcard-rg :tree-view
  "## TreeView"
  (apply-stiles
   [RawTreeView
    {:aria-label          "Fruit & Vegetables"}
    [RawTreeItem
     {:itemId "1"
      :label  "Fruit"}
     [RawTreeItem
      {:itemId "2"
       :label  "Apple"}]
     [RawTreeItem
      {:itemId "3"
       :label  "Orange"}
      [RawTreeItem
       {:itemId "4"
        :label  "Blood Orange"}]
      [RawTreeItem
       {:itemId "5"
        :label  "Naval Orange"}]
      [RawTreeItem
       {:itemId "6"
        :label  "Mandarin"}]]
     [RawTreeItem
      {:itemId "7"
       :label  "Pears"}]]
    [RawTreeItem
     {:itemId "8"
      :label  "Vegetables"}
     [RawTreeItem
      {:itemId "9"
       :label  "Tomato"}
      [RawTreeItem
       {:itemId "10"
        :label  "Cherry Tomato"}]
      [RawTreeItem
       {:itemId "11"
        :label  "Oxheart Tomato"}]
      [RawTreeItem
       {:itemId "12"
        :label  "Plum Tomato"}]]
     [RawTreeItem
      {:itemId "13"
       :label  "Cucumber"}]]]))