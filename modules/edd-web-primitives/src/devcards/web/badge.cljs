(ns web.badge
  (:require
   [devcards.core :refer-macros (defcard-rg)]

   [web.primitives.components :refer [RawBadge RawGrid]]
   [web.primitives.icons.core :refer [NotificationsNoneIcon]]
   [web.primitives.utils :refer [apply-stiles]]))

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
    [RawGrid {}
     [RawBadge
      {:id "badge"
       :color "error"
       :anchorOrigin {:vertical "bottom"
                      :horizontal "right"}
       :content [NotificationsNoneIcon]
       :badge-content "9+"}]]
    [RawGrid {}
     [RawBadge
      {:id "badge"
       :color "error"
       :anchorOrigin {:vertical "bottom"
                      :horizontal "right"}
       :content [NotificationsNoneIcon]
       :badge-content "5"}]]]))