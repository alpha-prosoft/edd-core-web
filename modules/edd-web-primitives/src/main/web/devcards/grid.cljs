(ns web.devcards.grid
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawGrid]]))

(defcard-rg :grid
  "## Grid"
  (let [item-style {:background-color "#c5c3c3"}
        content-props {:style {:background-color "#c5c3c3"
                               :border "1px solid #656565"
                               :padding 5
                               :text-align "center"}}]
    [RawGrid {:container true  :style {:padding "1rem"}}
     [RawGrid {:item true :xs 12
               :style {:text-align "center" :background-color "#ececec"}}
      "Container"]

     [RawGrid {:container true :item true :xs 12}
      [RawGrid {:item true :xs 1 :style item-style}
       [RawGrid content-props "xs 1"]]
      [RawGrid {:item true :xs 2 :style item-style}
       [RawGrid content-props "xs 2"]]
      [RawGrid {:item true :xs 3 :style item-style}
       [RawGrid content-props "xs 3"]]
      [RawGrid {:item true :xs 6 :style item-style}
       [RawGrid content-props "xs 6"]]]]))

(defcard-rg :grid-with-spacing
  "## Grid with spacing"
  (let [content-props {:style {:background-color "#c5c3c3"
                               :border "1px solid #656565"
                               :padding 5
                               :text-align "center"}}]

    [RawGrid {:container true :spacing 2 :justify-content "space-between" :alignContent "space-between"
              :style {:background-color "#ececec"}}
     [RawGrid {:item true :xs 1}
      [RawGrid content-props "xs 1"]]
     [RawGrid {:item true :xs 2}
      [RawGrid content-props "xs 2"]]
     [RawGrid {:item true :xs 3}
      [RawGrid content-props "xs 3"]]
     [RawGrid {:item true :xs 6}
      [RawGrid content-props "xs 6"]]]))
