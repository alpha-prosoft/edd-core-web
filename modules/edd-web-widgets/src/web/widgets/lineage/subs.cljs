(ns web.widgets.lineage.subs
  (:require [re-frame.core :as rf]
            [web.widgets.lineage.db :as db]))

(defn- with-person
  [db {:keys [metadata] :as entry}]
  (let [user
        (get-in db (db/user-path (:user-id metadata)))

        person
        (or (:name user) (:email user) (:email metadata))

        someone?
        (or person (and (some? (:user-id metadata)) (not (string? (:user-id metadata)))))]
    (assoc entry
           :person person
           :actor  (when someone?
                     {:user-id (:user-id metadata)
                      :name    (:name user)
                      :email   (or (:email user) (:email metadata))}))))

(defn stream
  "Status (:loading | :loaded | :failed), the failed response and the entries
   of a stream, newest first, each with the :person behind it (name, else email) and
   the :actor {:user-id :name :email} for an application that names people its own way (also when only
   the user id is known); both nil for an entry nobody did (written by a service, its name as user id)."
  [db [_ ref]]
  (let [{:keys [status entries response exists?]}
        (get-in db (db/stream-path ref))]
    {:status (or status :loading)
     :exists? (boolean exists?)
     :response response
     :entries (vec (sort-by #(get-in % [:metadata :date])
                            #(compare %2 %1)
                            (map #(with-person db %) entries)))}))

(defn refs
  "The refs a history is read from: its own slice, then the fallback slice when there is one."
  [scope slice fallback-slice]
  (cond-> [{:scope scope :slice slice}]
    fallback-slice (conj {:scope scope :slice fallback-slice})))

(defn first-with-entries
  "The stream that is shown of several (each as `stream` answers it): the first one, unless the lineage service
  has no such stream at all; then the next one, marked `:fallback?`. Streams are never merged."
  [streams]
  (let [[own & others]
        streams]
    (if (and (= :loaded (:status own)) (not (:exists? own)) (seq others))
      (assoc (first-with-entries others) :fallback? true)
      own)))

(defn shown
  [db [_ refs]]
  (first-with-entries (mapv #(stream db [::stream %]) refs)))

(rf/reg-sub ::stream stream)
(rf/reg-sub ::shown shown)
