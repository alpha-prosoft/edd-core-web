(ns web.devcards.form-select
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [reagent.core :as r]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawFormSelect RawMenuItem]]))

(def list-options
  [{:id 1 :name "John" :last-name "Smith"}
   {:id 2 :name "Oliver" :last-name "Jake"}
   {:id 3 :name "Jack" :last-name "Connor"}
   {:id 4 :name "Harry" :last-name "Callum"}
   {:id 5 :name "Jacob" :last-name "Jacob"}
   {:id 6 :name "Charlie" :last-name "Kyle"}])

(defcard-rg :select
  "## Select"
  (fn [data-atom _]
    (apply-stiles
     [RawFormSelect
      {:id (str ::form-select)
       :children   (mapv
                    #(r/as-element
                      (RawMenuItem {:id (:id %) :key (:id %) :value (:id %)}
                                   (str (:last-name %) " " (:name %)))) list-options)
       :input-label "Select name"
       :value     (:selected @data-atom)
       :on-change #(swap! data-atom merge {:selected (.. % -target -value)})}]))
  (r/atom {:selected ""}))
