(ns web.widgets.folder.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.widgets.folder.db :as db]
            [web.widgets.folder.events :as events]
            [web.widgets.folder.subs :as subs]))

(def props
  {:service :storage-svc
   :folder {:kind :room :id "room-1"}
   :spec (events/images-spec [:photos])
   :call-fx :app/call})

(def file
  #js {:name "hall.jpg" :type "image/jpeg" :size 2048})

(def upload
  {:entry :photos
   :item "item-1"
   :file file
   :started 1
   :on-uploaded [:app/photo-added]})

(deftest folder-of-pictures-is-public-lists
  (is
   (= [{:name :logo :list true :public true}
       {:name :cover :list true :public true}]
      (events/images-spec [:logo :cover]))))

(deftest saving-sets-the-folder-up
  (is
   (= [[:app/call {:service :storage-svc
                   :commands [{:cmd-id :folder->save
                               :folder {:kind :room :id "room-1"}
                               :attrs {:spec [{:name :photos :list true :public true}]}}]
                   :on-success [:app/saved]
                   :on-failure [::events/ignored]}]]
      (:fx (events/save {} [::events/save props {:on-success [:app/saved]}])))))

(deftest the-application-is-named-when-given
  (is
   (= :app-1
      (get-in (events/save {} [::events/save (assoc props :application-id :app-1)])
              [:fx 0 1 :commands 0 :application-id]))))

(deftest retain-keeps-the-folder-and-what-the-thing-uses
  (is
   (= [:folder->save :folder->retain]
      (mapv :cmd-id (get-in (events/retain {} [::events/retain props ["https://cdn/a"]]) [:fx 0 1 :commands]))))

  (is
   (= {:urls ["https://cdn/a"]}
      (get-in (events/retain {} [::events/retain props '("https://cdn/a")]) [:fx 0 1 :commands 1 :attrs]))))

(deftest an-upload-adds-the-file-in-the-folder-it-sets-up
  (let [{:keys [db fx]}
        (events/upload {:db {}} [::events/upload props upload])]

    (is
     (= [{:cmd-id :folder->save
          :folder {:kind :room :id "room-1"}
          :attrs {:spec [{:name :photos :list true :public true}]}}
         {:cmd-id :folder->add-file
          :folder {:kind :room :id "room-1"}
          :attrs {:entry :photos
                  :item "item-1"
                  :file-name "hall.jpg"
                  :content-type "image/jpeg"}}]
        (get-in fx [0 1 :commands])))

    (is
     (= [[:photos "hall.jpg" :uploading]]
        (mapv (juxt :entry #(.-name (:file %)) :status)
              (subs/uploads db [::subs/uploads props :photos]))))))

(deftest the-file-goes-to-its-presigned-url
  (testing "the upload URL is asked for with the size of the file"
    (is
     (= {:query-id :folder->upload-url
         :folder {:kind :room :id "room-1"}
         :entry :photos
         :item "item-1"
         :content-length 2048}
        (get-in (events/added {} [::events/added props upload]) [:fx 0 1 :query]))))

  (testing "the bytes go to the URL, then the public URL is handed on"
    (let [[[effect request]]
          (:fx (events/url-received {} [::events/url-received props upload {:result {:url "https://s3/put"
                                                                                     :public-url "https://cdn/p"}}]))]
      (is
       (= [::events/put "https://s3/put" [::events/stored props upload "https://cdn/p"]]
          [effect (:url request) (:on-success request)])))))

(deftest a-stored-upload-is-done
  (let [db
        (assoc-in {} (db/upload-path props "item-1") upload)

        {db-after :db fx :fx}
        (events/stored {:db db} [::events/stored props upload "https://cdn/p"])]

    (is
     (= []
        (subs/uploads db-after [::subs/uploads props :photos])))

    (is
     (= [[:dispatch [:app/photo-added "https://cdn/p"]]]
        fx))))

(deftest a-failed-upload-stays-until-dismissed
  (let [db
        (events/upload-failed (assoc-in {} (db/upload-path props "item-1") upload)
                              [::events/upload-failed props upload {:status 403}])]

    (is
     (= [[:failed {:status 403}]]
        (mapv (juxt :status :response) (subs/uploads db [::subs/uploads props :photos]))))

    (is
     (= []
        (subs/uploads (events/dismiss db [::events/dismiss props "item-1"]) [::subs/uploads props :photos])))))

(deftest folders-are-apart
  (let [db
        (assoc-in {} (db/upload-path props "item-1") upload)]
    (is
     (= []
        (subs/uploads db [::subs/uploads (assoc-in props [:folder :id] "room-2") :photos])))))
