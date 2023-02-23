(ns web.devcards.text-editor
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [reagent.core :as r]
   [web.primitives.components :refer [EddTextEditor]]
   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg :text-editor
  "## Text editor"
  (fn [data-atom _]
    (apply-stiles
     [EddTextEditor
      {:placeholder  "Please type here..."
       :set-contents (:value @data-atom)
       :editor-mode  :edit-mode
       :on-change    (fn [x] (swap! data-atom merge {:draft x}))}]))
  (r/atom {:draft "" :value "Content"}))

(defcard-rg :text-editor-with-action-buttons
  "## Text editor with action buttons"
  (fn [data-atom _]
    (apply-stiles
     [EddTextEditor
      {:placeholder  "Please type here..."
       :on-save      #(swap! data-atom merge {:value (:draft @data-atom)})
       :set-contents (:value @data-atom)
       :on-change    (fn [x] (swap! data-atom merge {:draft x}))}]))
  (r/atom {:draft "" :value "<a href='javascript:alert(1)'>Link with: javascript:alert(1)</a>"}))

(defcard-rg :text-editor-with-action-buttons-edit-icon-left
  "## Text editor with action buttons and edit button on the left side"
  (fn [data-atom _]
    (apply-stiles
     [EddTextEditor
      {:placeholder  "Please type here..."
       :on-save      #(swap! data-atom merge {:value (:draft @data-atom)})
       :set-contents (:value @data-atom)
       :on-change    (fn [x] (swap! data-atom merge {:draft x}))
       :edit-icon-position :left}]))
  (r/atom {:draft "" :value "<a href='javascript:alert(1)'>Link with: javascript:alert(1)</a>"}))

(defcard-rg :text-editor-disabled
  "## Text editor disabled"
  (apply-stiles
   [EddTextEditor
    {:placeholder  "Please type here..."
     :disable      true
     :set-contents "Content"}]))
