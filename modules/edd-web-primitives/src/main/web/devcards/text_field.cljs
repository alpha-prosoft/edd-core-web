(ns web.devcards.text-field
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [web.primitives.inputs.core :refer [RawTextField]]
   [web.primitives.icons.core :refer [ChatIcon]]

   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg text-field
  "## Text field"
  (apply-stiles [RawTextField {}]))

(defcard-rg text-field-disabled
  "## Text field disabled"
  (apply-stiles [RawTextField {:disabled true}]))

(defcard-rg text-field-with-prefix
  "## Text field with prefix"
  (apply-stiles [RawTextField {:label "Label" :prefix "Prefix" :default-value "value"}]))

(defcard-rg text-field-with-postfix
  "## Text field with postfix"
  (apply-stiles [RawTextField {:label "Label" :suffix "Suffix" :default-value "value"}]))

(defcard-rg text-field-with-postfix
  "## Text field with postfix"
  (apply-stiles [RawTextField {:label "Label" :prefix [ChatIcon] :default-value "value" :helperText "help text"}]))

(defcard-rg text-field-invalid
  "## Text field invalid"
  (apply-stiles [RawTextField {:error true :label "Label" :default-value "value" :helper-text "Invalid input"}]))

(defcard-rg text-field-read-only
  "## Text field read only"
  (apply-stiles [RawTextField {:read-only true :label "Label" :default-value "value"}]))
