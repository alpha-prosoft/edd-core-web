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
  (apply-stiles [RawTextField {:label "Label" :prefix "Prefix" :default-value "value" :input-props {:sx {:color "#654654"}}}]))

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

(defcard-rg :text-field-read-only-with-underline
  "## Text field read only with underline"
  (apply-stiles [RawTextField {:read-only-with-underline true :label "Label" :default-value "value"}]))

(defcard-rg :number-field
  "## Number field"
  (apply-stiles [RawNumberField {:label "Label"
                                 :default-value 123400
                                 :on-change (fn [event] (print "event: " event))}]))

(defcard-rg :number-field-custom-separator
  "## Number field with custom separator"
  (apply-stiles [RawNumberField {:label "Label"
                                 :default-value 123400
                                 :on-change (fn [event] (print "event: " event))
                                 :separator ","}]))

(defcard-rg :number-field-custom-formatting-func
  "## Number field with custom formatting function"
  (apply-stiles [RawNumberField {:label "Replaces 9 by 0"
                                 :default-value 1234
                                 :on-change (fn [event] (print "event: " event))
                                 :formatting-func formatting-func}]))

(defcard-rg :number-field-read-only-string
  "## Number field supports Strings on read-only"
  (apply-stiles [RawNumberField {:label "Auto-formatted value or String possible"
                                 :default-value "No value-String"
                                 :read-only true}]))

(defcard-rg :number-field-read-only-with-underline
  "## Number field with custom formatting function"
  (apply-stiles [RawNumberField {:label "Replaces 9 by 0"
                                 :default-value 1234
                                 :read-only-with-underline true}]))

(defcard-rg :percent-field
  "## Percent field"
  (apply-stiles [RawPercentField {:label "Label"
                                  :suffix "%"
                                  :default-value 0.2575
                                  :on-change (fn [event] (print "event: " event))}]))

(defcard-rg :percent-field-read-only-with-underline
  "## Percent field read only with underline"
  (apply-stiles [RawPercentField {:label "Label"
                                  :suffix "%"
                                  :read-only-with-underline true
                                  :default-value 0.2575}]))

(defcard-rg :text-field-with-input-props
  "## Text Field with input-props"
  (apply-stiles [RawTextField {:label "Label" :default-value "value" :input-props {:sx {:color "#da2374"}}}]))