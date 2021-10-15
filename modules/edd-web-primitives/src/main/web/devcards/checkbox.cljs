(ns web.devcards.checkbox
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [reagent.core :as r]

   [web.devcards.utils :refer [apply-stiles]]

   [web.primitives.components :refer [RawCheckbox]]))

(defcard-rg :checkbox
  "## Checkbox"
  (fn [data-atom _]
    (apply-stiles
     [RawCheckbox
      {:checked (:checked @data-atom)
       :label ""
       :on-change (fn [x] (swap! data-atom merge {:checked (not (:checked @data-atom))}))}]))
  (r/atom {:checked false}))

(defn- handle-state [atom]
  (if (= atom {:checked true :indeterminate false})
    {:checked true :indeterminate true}
    (if (= atom {:checked true :indeterminate true})
      {:checked false :indeterminate false}
      {:checked true :indeterminate false})))

(defcard-rg :checkbox-with-indeterminate-state
  "## Checkbox with indeterminate state and lable"
  (fn [data-atom _]
    (apply-stiles [RawCheckbox
                   {:indeterminate (:indeterminate @data-atom)
                    :checked (:checked @data-atom)
                    :on-change (fn [x] (swap! data-atom merge (handle-state @data-atom)))
                    :label "Label"}]))
  (r/atom {:checked true :indeterminate true}))

(defcard-rg :checkbox-with-label-placement-start
  "## Checkbox with label-placement on start"
  (fn [data-atom _]
    (apply-stiles [RawCheckbox
                   {:indeterminate (:indeterminate @data-atom)
                    :checked (:checked @data-atom)
                    :label-placement "start"
                    :on-change (fn [x] (swap! data-atom merge (handle-state @data-atom)))
                    :label "Label"}]))
  (r/atom {:checked true :indeterminate true}))

(defcard-rg :checkbox-disabled
  "## Checkbox disabled"
  (fn [data-atom _]
    (apply-stiles [RawCheckbox
                   {:checked true
                    :disabled true
                    :on-change (fn [x] (swap! data-atom merge (handle-state @data-atom)))
                    :label "Disabled Checkbox"}]))
  (r/atom {:checked true :indeterminate true}))


