(ns web.zoom
  (:require
   [reagent.core :as r]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawCheckbox RawZoom]]))

(defcard-rg :zoom
  "## Zoom"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true}
      [RawGrid {:size 12}
       [RawCheckbox {:checked (:show @data-atom)
                     :on-change (fn [] (swap! data-atom merge {:show (not (:show @data-atom))}))
                     :label "Show"}]]
      [RawGrid {:size 12}
       [RawZoom
        {:in (:show @data-atom)}
        [RawGrid {:container true :sx {:background-color "#edeeee"}} "Hello there!"]]]]))

  (r/atom {:show false}))