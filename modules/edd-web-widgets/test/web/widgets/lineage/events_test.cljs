(ns web.widgets.lineage.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [edd.client :as client]
            [web.widgets.lineage.events :as events]
            [web.widgets.lineage.subs :as subs]))

(def ref
  {:scope :bookings :slice [:booking-1 :user-1]})

(def stream
  {:service :lineage-svc
   :user-service :user-svc
   :application-id :app-1
   :ref ref})

(def entries
  [{:kind :requested :message "Requested" :metadata {:date 100 :user-id :user-1 :email "member@example.com"}}
   {:kind :confirmed :message "Confirmed" :metadata {:date 200 :user-id "booking-svc"}}])

(defn- shown
  [db]
  (subs/stream db [::subs/stream ref]))

(deftest loading-asks-the-lineage-service
  (testing "through edd.client unless the application gives its own call"
    (is
     (= [::client/call {:service :lineage-svc
                        :query {:query-id :lineage->get-by-identity
                                :ref ref
                                :application-id :app-1}}]
        (update (first (:fx (events/load {:db {}} [::events/load stream]))) 1 select-keys [:service :query]))))

  (is
   (= :app/call
      (ffirst (:fx (events/load {:db {}} [::events/load (assoc stream :call-fx :app/call)])))))

  (is
   (= :loading
      (:status (shown (:db (events/load {:db {}} [::events/load stream])))))))

(deftest loaded-entries-are-newest-first-with-their-people
  (let [{:keys [db fx]}
        (events/loaded {:db {}} [::events/loaded stream {:result {:entries entries}}])

        with-users
        (events/users-loaded db [::events/users-loaded {:result [{:id :user-1 :attrs {:name "Mia"}}]}])]

    (testing "only people are looked up, not the service that wrote an entry"
      (is
       (= {:query-id :get-by-id :id [:user-1]}
          (get-in (first fx) [1 :query]))))

    (is
     (= [["Confirmed" nil] ["Requested" "Mia"]]
        (mapv (juxt :message :person) (:entries (shown with-users)))))))

(deftest nothing-found-is-an-empty-history
  (let [{:keys [db]}
        (events/failed {:db {}} [::events/failed stream {:result nil}])]
    (is
     (= [:loaded false []]
        ((juxt :status :exists? :entries) (shown db))))))

(deftest a-fallback-stream-shows-only-when-the-own-one-does-not-exist
  (let [own
        {:status :loaded :exists? false :entries []}

        older
        {:status :loaded :exists? true :entries [{:message "Earlier"}]}]

    (is
     (= {:status :loaded :exists? true :entries [{:message "Earlier"}] :fallback? true}
        (subs/first-with-entries [own older])))

    (is
     (= (assoc own :exists? true)
        (subs/first-with-entries [(assoc own :exists? true) older])))))
