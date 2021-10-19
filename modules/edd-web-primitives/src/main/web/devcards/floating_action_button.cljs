(ns web.devcards.floating-action-button
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawGrid RawFab]]
   [web.primitives.icons.core :refer [AddIcon SaveIcon ClearIcon]]))

(defcard-rg :floating-action-button-add
  "## Floating Action Button Add"
  (apply-stiles
   [RawFab
    {:id       "add-button"
     :on-click (fn [] (print "on-click"))}
    [AddIcon]]))

(defcard-rg :floating-action-button-save
  "## Floating Action Button Save"
  (apply-stiles
   [RawFab
    {:id       "save-button"
     :on-click (fn [] (print "on-click"))}
    [SaveIcon]
    "save"]))

(defcard-rg :floating-action-button-disappear-on-click
  "## Floating Action Button disappear on click"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:item true :xs 2}
       (when
        (= 1 (:show @data-atom)) [RawFab
                                  {:id       "add-button"
                                   :on-click #(swap! data-atom merge {:show 2})}
                                  [ClearIcon]])]
      [RawGrid {:item true :xs 2}
       (when
        (= 2 (:show @data-atom)) [RawFab
                                  {:id       "add-button"
                                   :on-click #(swap! data-atom merge {:show 1})}
                                  [ClearIcon]])]]))

  (r/atom {:show 1}))
