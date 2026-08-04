(ns web.skeleton
  (:require
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawSkeleton]]
   [web.primitives.utils :refer [apply-stiles]]))

(defcard-rg :skeleton
  "## Skeleton"
  (apply-stiles [RawSkeleton
                 {:id "skeleton"
                  :height 200}]))
