(ns edd.events
  (:import goog.history.Html5History)
  (:require
   [re-frame.core :as rf]
   [bidi.bidi :as bidi]
   [edd.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [db [_ {:keys [selected-language show-language-switcher? config routes]
             :or   {selected-language       :en
                    show-language-switcher? false}}]]
   (-> db/default-db
       (merge db)
       (assoc-in [::db/selected-language] selected-language)
       (assoc-in [::db/show-language-switcher?] show-language-switcher?)
       (assoc ::db/config config)
       (assoc ::db/routes routes))))

(rf/reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ page & [params]]]
   {:db       (assoc db ::db/active-panel page
                     ::db/drawer false)
    :dispatch [(keyword (str "initialize-" (name page) "-db"))
               params]}))

(rf/reg-event-db
 ::toggle-drawer
 (fn [db _]
   (update db ::db/drawer #(not %))))

(rf/reg-event-db
 ::change-language
 (fn [db [_ value]]
   (assoc db ::db/selected-language value)))

(rf/reg-event-db
 :menu-toggle
 (fn [db event]
   (update-in db [::db/menu-expanded (second event)] #(not %))))

(rf/reg-event-db
 ::add-translation
 (fn [db [_ body]]
   (update-in db [::db/translations] #(merge % body))))

(rf/reg-event-fx
 ::navigate
 (fn [{:keys [db]} [_ target & [params]]]
   (let [routes (::db/routes db)
         url (::db/url db "/")
         new-url (if (keyword? target)
                   (bidi/path-for* routes target params)
                   target)
         {:keys [handler route-params]} (if (keyword? target)
                                          {:handler      target
                                           :route-params (or params {})}
                                          (bidi/match-route (::db/routes db) target))]
     (when (and (not= url new-url)
                (not
                 (get-in db [::db/config :mobile] false)))
       (.pushState (.-history js/window)
                   #js {}
                   ""
                   new-url))
     {:dispatch [(keyword (str "initialize-" (name handler) "-db"))
                 route-params]
      :db       (assoc db ::db/drawer false
                       ::db/url new-url
                       ::db/active-panel handler)})))

(rf/reg-event-db
 ::register-menu-item
 (fn [db [_ {:keys [key] :as item}]]
   (assoc db [::db/menu key] item)))

(rf/reg-event-db
 ::remove-user
 (fn [db]
   (assoc-in db [::db/user] nil)))


