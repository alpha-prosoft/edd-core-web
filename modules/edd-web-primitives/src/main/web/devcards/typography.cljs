(ns web.devcards.typography

  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.data-display.core :refer [RawTypography]]))

(defcard-rg :typography-h1
  "## Typography H1"
  (apply-stiles [RawTypography
                 {:id "h1"
                  :variant "h1"} "Text example. 1234567890"]))

(defcard-rg :typography-h2
  "## Typography H2"
  (apply-stiles [RawTypography
                 {:id "h2"
                  :variant "h2"} "Text example. 1234567890"]))

(defcard-rg :typography-h3
  "## Typography H3"
  (apply-stiles [RawTypography
                 {:id "h3"
                  :variant "h3"} "Text example. 1234567890"]))

(defcard-rg :typography-h4
  "## Typography H4"
  (apply-stiles [RawTypography
                 {:id "h4"
                  :variant "h4"} "Text example. 1234567890"]))

(defcard-rg :typography-h5
  "## Typography H5"
  (apply-stiles [RawTypography
                 {:id "h5"
                  :variant "h5"} "Text example. 1234567890"]))

(defcard-rg :typography-h6
  "## Typography H6"
  (apply-stiles [RawTypography
                 {:id "h6"
                  :variant "h6"} "Text example. 1234567890"]))

(defcard-rg :typography-subtitle1
  "## Typography subtitle1"
  (apply-stiles [RawTypography
                 {:id "subtitle1"
                  :variant "subtitle1"} "Text example. 1234567890"]))

(defcard-rg :typography-subtitle2
  "## Typography subtitle2"
  (apply-stiles [RawTypography
                 {:id "subtitle2"
                  :variant "subtitle2"} "Text example. 1234567890"]))

(defcard-rg :typography-body1
  "## Typography body1"
  (apply-stiles [RawTypography
                 {:id "body1"
                  :variant "body1"} "Text example. 1234567890"]))

(defcard-rg :typography-body2
  "## Typography body2"
  (apply-stiles [RawTypography
                 {:id "body2"
                  :variant "body2"} "Text example. 1234567890"]))

(defcard-rg :typography-caption
  "## Typography caption"
  (apply-stiles [RawTypography
                 {:id "caption"
                  :variant "caption"} "Text example. 1234567890"]))

(defcard-rg :typography-button
  "## Typography button"
  (apply-stiles [RawTypography
                 {:id "button"
                  :variant "button"} "Text example. 1234567890"]))

(defcard-rg :typography-overline
  "## Typography overline"
  (apply-stiles [RawTypography
                 {:id "overline"
                  :variant "overline"} "Text example. 1234567890"]))

(defcard-rg :typography-srOnly
  "## Typography srOnly"
  (apply-stiles [RawTypography
                 {:id "srOnly"
                  :variant "srOnly"} "Text example. 1234567890"]))

(defcard-rg :typography-inherit
  "## Typography inherit"
  (apply-stiles [RawTypography
                 {:id "inherit"
                  :variant "inherit"} "Text example. 1234567890"]))
