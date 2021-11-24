(ns web.devcards.formatting
  (:require
   [reagent.core :as r]
   [cljsjs.react]
   [cljsjs.react.dom]
   [clojure.edn :as edn]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.formatting.core :as formatting]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawGrid RawTextField]]))

(defcard-rg :format-number
  "## Format Number"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true :spacing 2}
      [RawGrid {:item true :xs 3}
       [RawTextField {:label "Amount in default (millions)"
                      :value (formatting/format-number (:number @data-atom))
                      :read-only true}]
       [RawTextField {:label "Amount in millions"
                      :value (formatting/format-number (:number @data-atom) :mn)
                      :read-only true}]
       [RawTextField {:label "Amount in thousands"
                      :value (formatting/format-number (:number @data-atom) :tsd)
                      :read-only true}]
       [RawTextField {:label "Amount in full scale"
                      :value (formatting/format-number (:number @data-atom) :full)
                      :read-only true}]]
      [RawGrid {:item true :container true :xs 9 :align-items "center" :justify-content "center"}
       [RawGrid {:item true :container true :xs 6 :spacing 2 :direction "column"}
        [RawGrid {:item true}
         [RawTextField {:label "Enter amount in cents"
                        :type "number"
                        :default-value (:number @data-atom)
                        :on-change #(swap! data-atom merge {:number (edn/read-string %)})}]]]]]))
  (r/atom {:number 1234567890}))

(defcard-rg :format-percent
  "## Format Percent"
  (fn [data-atom _]
    (apply-stiles
     [RawGrid {:container true :spacing 2}
      [RawGrid {:item true :xs 3}
       [RawTextField {:type "String"
                      :value (formatting/format-percent (:percent @data-atom))
                      :read-only true}]]
      [RawGrid {:item true :container true :xs 9 :align-items "center" :justify-content "center"}
       [RawGrid {:item true :container true :xs 6 :spacing 2 :direction "column"}
        [RawGrid {:item true}
         [RawTextField {:label "Enter percent in decimal notation"
                        :type "number"
                        :default-value  (:percent @data-atom)
                        :on-change #(swap! data-atom merge {:percent (edn/read-string %)})}]]]]]))
  (r/atom {:percent 0.2583}))