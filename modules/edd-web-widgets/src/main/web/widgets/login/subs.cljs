(ns web.widgets.login.subs
  (:require
   [edd.db :as edd-db]
   [web.widgets.login.db :as db]
   [re-frame.core :as rf]
   [clojure.string :refer [blank?]]
   [goog.crypt.base64 :as b64]
   [web.widgets.login.utils :refer [json-parser]]))

(rf/reg-sub
 :init-login-db
 (fn [db]
   (merge db db/default-db)))

(rf/reg-sub
 ::username
 (fn [db]
   (:db/username db)))

(rf/reg-sub
 ::password
 (fn [db]
   (:db/password db)))

(rf/reg-sub
 ::dialog-visible
 (fn [db]
   (or (::db/dialog-visible db) false)))

(rf/reg-sub
 ::form-type
 (fn [db]
   (::db/form-type db)))

(rf/reg-sub
 ::user-name
 (fn [db]
   (let [user (get-in db [::edd-db/user])
         decoded (b64/decodeString
                  (second
                   (clojure.string/split (:id-token user) #"\.")))]
     (when
      (some? user) (-> (json-parser false true decoded)
                       (:email))))))

(rf/reg-sub
 ::user
 (fn [db]
   (get-in db [::edd-db/user])))

(rf/reg-sub
 ::logged-in
 (fn [db]
   (some? (get-in db [::edd-db/user]))))

(rf/reg-sub
 ::confirmation-visible
 (fn [db]
   (::db/confirmation-visible db)))

(rf/reg-sub
 ::error-message-visible
 (fn [db]
   (get-in db [::db/error-message-visible] false)))

(rf/reg-sub
 ::error-message
 (fn [db]
   (get-in db [::db/error-message])))

(rf/reg-sub
 ::show-password?
 (fn [db]
   (::db/show-password? db)))

(defn validate-email [email]
  (re-matches #".+\@.+\..+" (str email)))

(rf/reg-sub
 ::username-invalid?
 (fn [db]
   (let [username (get-in db [::db/username] "")]
     (nil? (validate-email username)))))

(rf/reg-sub
 ::password-invalid?
 (fn [db]
   (let [password (str (get-in db [::db/password]))]
     (or
      (nil? (re-seq #"[A-Z]" password))
      (nil? (re-seq #"[a-z]" password))
      (nil? (re-seq #"[0-9]" password))
      (< (count password) 8)))))

(rf/reg-sub
 ::confirmation-code-empty?
 (fn [db]
   (blank? (get-in db [::db/confirmation-code]))))




