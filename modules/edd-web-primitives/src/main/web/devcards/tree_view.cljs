(ns web.devcards.tree-view
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [reagent.core :as r]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawTreeView RawTreeItem]]
   [web.primitives.icons.core :refer [ExpandMoreIcon ChevronRightIcon]]))

(defcard-rg :tree-view
  "## TreeView"
  (apply-stiles
   [RawTreeView
    {:aria-label          "Fruit & Vegetables"
     :defaultCollapseIcon (r/as-element [ExpandMoreIcon {}])
     :defaultExpandIcon   (r/as-element [ChevronRightIcon {}])}
    [RawTreeItem
     {:nodeId "1"
      :label  "Fruit"}
     [RawTreeItem
      {:nodeId "2"
       :label  "Apple"}]
     [RawTreeItem
      {:nodeId "3"
       :label  "Orange"}
      [RawTreeItem
       {:nodeId "4"
        :label  "Blood Orange"}]
      [RawTreeItem
       {:nodeId "5"
        :label  "Naval Orange"}]
      [RawTreeItem
       {:nodeId "6"
        :label  "Mandarin"}]]
     [RawTreeItem
      {:nodeId "7"
       :label  "Pears"}]]
    [RawTreeItem
     {:nodeId "8"
      :label  "Vegetables"}
     [RawTreeItem
      {:nodeId "9"
       :label  "Tomato"}
      [RawTreeItem
       {:nodeId "10"
        :label  "Cherry Tomato"}]
      [RawTreeItem
       {:nodeId "11"
        :label  "Oxheart Tomato"}]
      [RawTreeItem
       {:nodeId "12"
        :label  "Plum Tomato"}]]
     [RawTreeItem
      {:nodeId "13"
       :label  "Cucumber"}]]]))