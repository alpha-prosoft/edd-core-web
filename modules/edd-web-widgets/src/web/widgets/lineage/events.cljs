(ns web.widgets.lineage.events
  "History of one stream: its entries from the lineage service, the people
   behind them from the user service.

   A stream is {:ref {:scope :slice} :service :user-service :application-id
   :call-fx}. call-fx: the effect the application sends backend requests with
   (default: edd.client's); it takes {:service :query :on-success :on-failure}."
  (:require [edd.client :as client]
            [re-frame.core :as rf]
            [web.widgets.lineage.db :as db]))

(defn- awaiting
  "A stream that is shown already keeps its entries while it loads again."
  [stream]
  (if (= :loaded (:status stream))
    stream
    (assoc stream :status :loading)))

(defn- call
  [{:keys [call-fx]} request]
  [(or call-fx ::client/call) request])

(defn load
  [{:keys [db]} [_ {:keys [service application-id ref] :as stream}]]
  {:db (update-in db (db/stream-path ref) awaiting)
   :fx [(call stream {:service service
                      :query (cond-> {:query-id :lineage->get-by-identity
                                      :ref ref}
                               application-id (assoc :application-id application-id))
                      :on-success [::loaded stream]
                      :on-failure [::failed stream]})]})

(defn- unknown-user-ids
  "People behind the entries who are not loaded yet. An entry written by a service
   carries the service's name instead of a person's id: nobody to look up."
  [db entries]
  (let [user-ids
        (distinct (remove string? (keep #(get-in % [:metadata :user-id]) entries)))]
    (vec (remove #(get-in db (db/user-path %)) user-ids))))

(defn loaded
  [{:keys [db]} [_ {:keys [user-service ref] :as stream} {:keys [result]}]]
  (let [entries
        (vec (:entries result))

        user-ids
        (unknown-user-ids db entries)]
    {:db (assoc-in db (db/stream-path ref) {:status :loaded
                                            :exists? (some? result)
                                            :entries entries})
     :fx [(when (and user-service (seq user-ids))
            (call stream {:service user-service
                          :query {:query-id :get-by-id
                                  :id user-ids}
                          :on-success [::users-loaded]
                          :on-failure [::users-failed]}))]}))

(defn- nothing-found?
  "A query that answered nil ends as a failure in edd.client: there is no history yet."
  [response]
  (and (map? response)
       (contains? response :result)
       (nil? (:result response))))

(defn failed
  [{:keys [db] :as cofx} [_ {:keys [ref] :as stream} response]]
  (if (nothing-found? response)
    (loaded cofx [::loaded stream {:result nil}])
    {:db (update-in db (db/stream-path ref) merge {:status :failed
                                                   :response response})}))

(defn users-loaded
  "Users the caller may not see are missing from the answer; their entries
   show the email they carry."
  [db [_ {:keys [result]}]]
  (reduce (fn [with-users {:keys [id attrs]}]
            (assoc-in with-users (db/user-path id) attrs))
          db
          (if (map? result) [result] result)))

(defn users-failed
  [db _]
  db)

(rf/reg-event-fx ::load load)
(rf/reg-event-fx ::loaded loaded)
(rf/reg-event-fx ::failed failed)
(rf/reg-event-db ::users-loaded users-loaded)
(rf/reg-event-db ::users-failed users-failed)
