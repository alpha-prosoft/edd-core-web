(ns web.link
  (:require
   [devcards.core :refer-macros (defcard-rg)]
   [web.primitives.components :refer [RawLink]]
   [web.primitives.utils :refer [apply-stiles]]))

(defcard-rg :link
  "## Link"
  (apply-stiles [RawLink {} "link"]))
