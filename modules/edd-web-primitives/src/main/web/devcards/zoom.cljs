(ns web.devcards.zoom
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawCheckbox RawZoom]]))

(defcard-rg :zoom
  "## Zoom"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:container true :item true}
       [RawCheckbox {:checked (:show @data-atom)
                     :on-change (fn [] (swap! data-atom merge {:show (not (:show @data-atom))}))
                     :label "Show"}]]
      [RawGrid {:container true :item true}
       [RawZoom
        {:in (:show @data-atom)}
        [RawGrid {:container true :style {:background-color "#edeeee"}} "Hello there!"]]]]))

  (r/atom {:show false}))