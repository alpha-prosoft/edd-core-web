(ns web.link
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.components :refer [RawLink]]
   [web.utils :refer [apply-stiles]]))

(defcard-rg :link
  "## Link"
  (apply-stiles [RawLink {} "link"]))
