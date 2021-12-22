(ns projectname.home.subs
  (:require
   [projectname.home.db :as db]
   [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (::db/name db)))

(rf/reg-sub
 ::clicks
 (fn [db]
   (::db/clicks db)))

(rf/reg-sub
 ::items
 (fn [db]
   (::db/items db)))