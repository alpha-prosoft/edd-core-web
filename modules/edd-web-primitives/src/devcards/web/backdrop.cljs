(ns web.backdrop
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawBackdrop RawGrid RawCheckbox]]))

(defcard-rg :backdrop
  "## Backdrop"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:size 12}
       [RawCheckbox {:checked (:show @data-atom)
                     :on-change (fn [] (swap! data-atom merge {:show true}))
                     :label "Show"}]]
      [RawGrid {:size 12}
       [RawBackdrop
        {:open (:show @data-atom)
         :on-click #(swap! data-atom merge {:show false})}]]]))

  (r/atom {:show false}))