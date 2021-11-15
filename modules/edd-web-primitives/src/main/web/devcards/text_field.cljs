(ns web.devcards.text-field
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [web.primitives.components :refer [RawTextField RawNumberField RawPercentField]]
   [web.primitives.icons.core :refer [ChatIcon]]

   [clojure.string :as str]

   [devcards.core :refer-macros (defcard-rg)]
   [web.devcards.utils :refer [apply-stiles]]))

(defn formatting-func [value]
  (str/replace (str value) #"9" "0"))

(defcard-rg :text-field
  "## Text field"
  (apply-stiles [RawTextField {}]))

(defcard-rg :text-field-disabled
  "## Text field disabled"
  (apply-stiles [RawTextField {:disabled true}]))

(defcard-rg :text-field-with-prefix
  "## Text field with prefix"
  (apply-stiles [RawTextField {:label "Label" :prefix "Prefix" :default-value "value"}]))

(defcard-rg :text-field-with-postfix
  "## Text field with postfix"
  (apply-stiles [RawTextField {:label "Label" :suffix "Suffix" :default-value "value"}]))

(defcard-rg :text-field-with-postfix
  "## Text field with postfix"
  (apply-stiles [RawTextField {:label "Label" :prefix [ChatIcon] :default-value "value" :helperText "help text"}]))

(defcard-rg :text-field-invalid
  "## Text field invalid"
  (apply-stiles [RawTextField {:error true :label "Label" :default-value "value" :helper-text "Invalid input"}]))

(defcard-rg :text-field-read-only
  "## Text field read only"
  (apply-stiles [RawTextField {:read-only true :label "Label" :default-value "value"}]))

(defcard-rg :number-field
  "## Number field"
  (apply-stiles [RawNumberField {:label "Label"
                                 :default-value "1.234"
                                 :on-change (fn [event] (print "event: " event))}]))

(defcard-rg :number-field-custom-separator
  "## Number field with custom separator"
  (apply-stiles [RawNumberField {:label "Label"
                                 :default-value "1,234"
                                 :on-change (fn [event] (print "event: " event))
                                 :separator ","}]))

(defcard-rg :number-field-custom-formatting-func
  "## Number field with custom formatting function"
  (apply-stiles [RawNumberField {:label "Replaces 9 by 0"
                                 :default-value "1234"
                                 :on-change (fn [event] (print "event: " event))
                                 :formatting-func formatting-func}]))

(defcard-rg :percent-field
  "## Percent field"
  (apply-stiles [RawPercentField {:label "Label"
                                  :default-value "25,75"
                                  :on-change (fn [event] (print "event: " event))}]))