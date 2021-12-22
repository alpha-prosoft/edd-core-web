(ns web.widgets.translation.events
  (:require
   [goog.object :as g]
   [re-frame.core :as rf]
   [web.widgets.translation.db :as db]))

(rf/reg-event-db
 :initialize-translation-db
 (fn [{:keys [db]}]
   (merge db db/default-db)))

(rf/reg-event-fx
 ::init
 (fn [{:keys [db]} [_ {:keys [default-lang lang-list]
                       :or   {default-lang :en}}]]
   (let [initialized? (get-in db [::db/initialized?])]
     (when (not initialized?)
       (g/set js/window "lang" :default-lang))

     (if initialized?
       {}
       (merge
        {:db (->
              (merge
               db
               db/default-db)
              (assoc-in [::db/initialized?] true)
              (assoc-in [::db/selected-lang] default-lang))}
        (when (some? lang-list)
          {:dispatch [::set-lang-list lang-list]}))))))

(rf/reg-event-db
 ::set-lang-list
 (fn [db [_ lang-list]]
   (assoc-in db [::db/lang-list] lang-list)))

(rf/reg-event-db
 ::change-lang
 (fn [db [_ lang]]
   (assoc-in db [::db/selected-lang] (keyword lang))))