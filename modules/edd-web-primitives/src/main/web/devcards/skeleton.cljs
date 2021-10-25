(ns web.devcards.skeleton
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawSkeleton]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg :skeleton
  "## Skeleton"
  (apply-stiles [RawSkeleton
                 {:id "skeleton"
                  :height 200}]))
