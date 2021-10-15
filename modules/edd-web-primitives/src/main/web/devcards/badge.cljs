(ns web.devcards.badge
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawBadge RawGrid]]
   [web.primitives.icons.core :refer [NotificationsNoneIcon]]
   [web.devcards.utils :refer [apply-stiles]]))

(defcard-rg :badge
  "## Badge"
  (apply-stiles [RawBadge
                 {:id "badge"
                  :content "Amount of something"
                  :badge-content 15}]))

(defcard-rg :notifications-with-badge
  "## Notifications with Badge"
  (apply-stiles
   [RawGrid {:container true :spacing 2}
    [RawGrid {:item true}
     [RawBadge
      {:id "badge"
       :color "error"
       :anchorOrigin {:vertical "bottom"
                      :horizontal "right"}
       :content [NotificationsNoneIcon]
       :badge-content "9+"}]]
    [RawGrid {:item true}
     [RawBadge
      {:id "badge"
       :color "error"
       :anchorOrigin {:vertical "bottom"
                      :horizontal "right"}
       :content [NotificationsNoneIcon]
       :badge-content "5"}]]]))