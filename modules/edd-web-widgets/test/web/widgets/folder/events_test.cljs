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
   :on-uploaded [:app/photo-added]})

(defn- started
  "db after uploads of these items started, in this order."
  [& items]
  (reduce (fn [db item]
            (:db (events/upload {:db db} [::events/upload props (assoc upload :item item)])))
          {}
          items))

(defn- ended
  [db event-fn event-id item & args]
  (event-fn {:db db} (into [event-id props (get-in db (db/upload-path props item))] args)))

(deftest folder-of-pictures-is-public-lists
  (is
   (= [{:name :logo :list true :public true :types ["image/jpeg" "image/png" "image/webp" "image/gif"]}
       {:name :cover :list true :public true :types ["image/jpeg" "image/png" "image/webp" "image/gif"]}]
      (events/images-spec [:logo :cover]))))

(deftest saving-sets-the-folder-up
  (is
   (= [[:app/call {:service :storage-svc
                   :commands [{:cmd-id :folder->save
                               :folder {:kind :room :id "room-1"}
                               :attrs {:spec [{:name :photos :list true :public true :types ["image/jpeg" "image/png" "image/webp" "image/gif"]}]}}]
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
          :attrs {:spec [{:name :photos :list true :public true :types ["image/jpeg" "image/png" "image/webp" "image/gif"]}]}}
         {:cmd-id :folder->add-file
          :folder {:kind :room :id "room-1"}
          :attrs {:entry :photos
                  :item "item-1"
                  :file-name "hall.jpg"
                  :content-type "image/jpeg"}}]
        (get-in fx [0 1 :commands])))

    (is
     (= [[:photos "hall.jpg" :uploading]]
        (mapv (juxt :entry :file-name :status)
              (subs/uploads db [::subs/uploads props :photos]))))))

(deftest the-file-name-goes-with-every-step
  (let [db
        (started "item-1")

        kept
        (get-in db (db/upload-path props "item-1"))

        failed
        (:db (ended db events/upload-failed ::events/upload-failed "item-1" {:status 500}))]

    (is
     (= "hall.jpg"
        (get-in (events/added {} [::events/added props kept]) [:fx 0 1 :on-failure 2 :file-name])))

    (is
     (= [["hall.jpg" :failed]]
        (mapv (juxt :file-name :status) (subs/uploads failed [::subs/uploads props :photos]))))))

(deftest the-checked-type-is-sent
  (let [{:keys [fx]}
        (events/upload {:db {}} [::events/upload props (assoc upload
                                                              :file #js {:name "hall.jpg" :type "" :size 10}
                                                              :content-type "image/png")])]
    (is
     (= "image/png"
        (get-in fx [0 1 :commands 1 :attrs :content-type])))))

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
          [effect (:url request) (:on-success request)]))))

  (testing "the PUT sends the headers the URL was signed for"
    (let [headers
          {"Content-Type" "image/jpeg"
           "Cache-Control" "public, max-age=31536000, immutable"}

          [[effect request]]
          (:fx (events/url-received {} [::events/url-received (assoc props :put-fx :app/put) upload
                                        {:result {:url "https://s3/put"
                                                  :headers headers}}]))]
      (is
       (= [:app/put headers]
          [effect (:headers request)])))))

(deftest a-stored-upload-is-done
  (let [{db-after :db fx :fx}
        (ended (started "item-1") events/stored ::events/stored "item-1" "https://cdn/p")]

    (is
     (= []
        (subs/uploads db-after [::subs/uploads props :photos])))

    (is
     (= [[:dispatch [:app/photo-added "https://cdn/p"]]]
        fx))))

(deftest pictures-come-in-the-order-they-were-chosen
  (let [db
        (started "first" "second" "third")

        second-stored
        (ended db events/stored ::events/stored "second" "https://cdn/2")

        first-failed
        (ended (:db second-stored) events/upload-failed ::events/upload-failed "first" {:status 500})

        third-stored
        (ended (:db first-failed) events/stored ::events/stored "third" "https://cdn/3")]

    (is
     (= []
        (:fx second-stored)))

    (is
     (= [[:dispatch [:app/photo-added "https://cdn/2"]]]
        (:fx first-failed)))

    (is
     (= [[:dispatch [:app/photo-added "https://cdn/3"]]]
        (:fx third-stored)))))

(deftest a-function-gets-the-url
  (let [db
        (:db (events/upload {:db {}} [::events/upload props (assoc upload :on-uploaded identity)]))]
    (is
     (= [[::events/callback [identity "https://cdn/p"]]]
        (:fx (ended db events/stored ::events/stored "item-1" "https://cdn/p"))))))

(deftest a-save-waits-for-the-uploads
  (let [db
        (started "first" "second")

        waiting
        (events/when-uploaded {:db db} [::events/when-uploaded props [:app/save]])

        first-stored
        (ended (:db waiting) events/stored ::events/stored "first" "https://cdn/1")

        second-failed
        (ended (:db first-stored) events/upload-failed ::events/upload-failed "second" {:status 500})]

    (is
     (= [2 1 0]
        (mapv #(subs/uploading % [::subs/uploading props]) [db (:db first-stored) (:db second-failed)])))

    (is
     (nil?
      (:fx waiting)))

    (is
     (= [[:dispatch [:app/photo-added "https://cdn/1"]]]
        (:fx first-stored)))

    (is
     (= [[:dispatch [:app/save]]]
        (:fx second-failed)))

    (is
     (= [[:dispatch [:app/save]]]
        (:fx (events/when-uploaded {:db (:db second-failed)} [::events/when-uploaded props [:app/save]]))))))

(deftest a-save-of-another-folder-does-not-wait
  (is
   (= [[:dispatch [:app/save]]]
      (:fx (events/when-uploaded {:db (started "item-1")}
                                 [::events/when-uploaded (assoc-in props [:folder :id] "room-2") [:app/save]])))))

(deftest a-failed-upload-stays-until-dismissed
  (let [db
        (:db (ended (started "item-1") events/upload-failed ::events/upload-failed "item-1" {:status 403}))]

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
