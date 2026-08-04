(ns web.grid
  (:require
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawGrid]]))

(defcard-rg :grid
  "## Grid"
  (let [item-style {:background-color "#c5c3c3"}
        content-props {:sx {:background-color "#c5c3c3"
                            :border           "1px solid #656565"
                            :padding          "5px"
                            :text-align       "center"}}]
    [RawGrid {:container true}
     [RawGrid {:size 12
               :sx   {:text-align "center"
                      :background-color "#ececec"}}
      "Container"]
     [RawGrid {:size 12}
      [RawGrid {:container true}
       [RawGrid {:size 1 :sx item-style}
        [RawGrid content-props ":size 1"]]
       [RawGrid {:size 2 :sx item-style}
        [RawGrid content-props ":size 2"]]
       [RawGrid {:size 3 :sx item-style}
        [RawGrid content-props ":size 3"]]
       [RawGrid {:size 6 :sx item-style}
        [RawGrid content-props ":size 6"]]]]]))

(defcard-rg :grid-with-spacing
  "## Grid with spacing"
  (let [content-props {:sx {:background-color "#c5c3c3"
                            :border           "1px solid #656565"
                            :padding          "5px"
                            :text-align       "center"}}]

    [RawGrid {:container true
              :spacing 2
              :justify-content "space-between"
              :alignContent "space-between"
              :sx     {:background-color "#ececec"}}
     [RawGrid {:size {:xs 1 :md 3 :lg 6}}
      [RawGrid content-props ":size {:xs 1 :md 3 :lg 6}"]]
     [RawGrid {:size {:xs 2 :md 3 :lg 3}}
      [RawGrid content-props ":size {:xs 2 :md 3 :lg 3}"]]
     [RawGrid {:size {:xs 3 :md 3 :lg 2}}
      [RawGrid content-props ":size {:xs 3 :md 3 :lg 2}"]]
     [RawGrid {:size {:xs 6 :md 3 :lg 1}}
      [RawGrid content-props ":size {:xs 6 :md 3 :lg 1}"]]]))

(defcard-rg :grid-with-spacing
  "## Grid with spacing 1"
  (let [content-props {:sx {:background-color "#c5c3c3"
                            :border           "1px solid #656565"
                            :padding "5px"
                            :text-align "center"}}]

    [RawGrid {:container true
              :spacing 2
              :sx {:background-color "#b0f7cf"
                   :padding-bottom "16px"
                   :padding-right "16px"}}
     [RawGrid {:container true
               :size 12
               :column-spacing 2
               :justify-content "space-between"
               :alignContent "space-between"
               :sx        {:background-color "#ececec"}}
      [RawGrid {:size 1}
       [RawGrid content-props ":size 1"]]
      [RawGrid {:size 2}
       [RawGrid content-props ":size 2"]]
      [RawGrid {:size 3}
       [RawGrid content-props ":size 3"]]
      [RawGrid {:size 6}
       [RawGrid content-props ":size 6"]]]]))

(defcard-rg :grid-with-spacing
  "## Grid with spacing"
  (let [content-props {:sx {:background-color "#c5c3c3"
                            :border           "1px solid #656565"
                            :padding          "5px"
                            :text-align       "center"}}]

    [RawGrid {:container true
              :row-spacing 2
              :direction "column"
              :sx        {:background-color "#ececec"
                          :padding "0 16px 16px 16px"}}
     [RawGrid {:size 1}
      [RawGrid content-props ":size 1"]]
     [RawGrid {:size 4}
      [RawGrid content-props ":size 4"]]
     [RawGrid {:size 12}
      [RawGrid content-props ":size 12"]]
     [RawGrid {:size 6}
      [RawGrid content-props ":size 6"]]]))
