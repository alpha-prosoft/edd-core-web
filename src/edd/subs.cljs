(ns edd.subs
  (:require
   [edd.db :as db]
   [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))

(rf/reg-sub
 ::active-panel
 (fn [db]
   (::db/active-panel db)))

(rf/reg-sub
 ::drawer
 (fn [db]
   (::db/drawer db)))

(rf/reg-sub
 ::ready
 (fn [db]
   (::db/ready db)))

(rf/reg-sub
 ::menu-expanded
 (fn [db]
   (::db/menu-expanded db)))

(rf/reg-sub
 ::i18n
 (fn [db]
   (::db/i18n db)))

(rf/reg-sub
 ::selected-language
 (fn [db]
   (::db/selected-language db)))

(rf/reg-sub
 ::translations
 (fn [db]
   (::db/translations db)))

(rf/reg-sub
 ::config
 (fn [db]
   (::db/config db)))

(rf/reg-sub
 ::menu-items
 (fn [db]
   (::db/menu-items db)))

(rf/reg-sub
 ::logged-in
 (fn [db]
   (some? (get-in db [::db/user]))))

(rf/reg-sub
 ::show-language-switcher?
 (fn [db]
   (get-in db [::db/show-language-switcher?])))

(rf/reg-sub
 ::get-application-roles
 (fn [db]
   (reduce
    (fn [p v]
      (assoc p v true))
    {}
    (get-in db [::db/application :attrs :my-roles]))))
