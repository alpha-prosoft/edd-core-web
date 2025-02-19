(ns web.number-field
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [web.primitives.components :refer [RawNumberField RawPercentField]]

   [clojure.string :as str]

   [devcards.core :refer-macros (defcard-rg)]
   [web.utils :refer [apply-stiles]]))

(defn formatting-func [value]
  (str/replace (str value) #"9" "0"))

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

(defcard-rg :number-field-auto-focus
  "## Number field with autoFocus"
  (apply-stiles [RawNumberField {:label "Label"
                                 :default-value 123400
                                 :auto-focus true
                                 :on-change (fn [event] (print "event: " event))}]))

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
