(ns edd.events
  (:import goog.history.Html5History)
  (:require
   [re-frame.core :as rf]
   [reitit.frontend :as reitit]
   [edd.client :as client]
   [edd.db :as db]))

(rf/reg-event-fx
 ::application-loaded
 (fn [{:keys [db]} [_ do-after-load {:keys [result]}]]
   {:db (-> db
            (assoc ::db/application result)
            (assoc-in [::db/config :ApplicationId]
                      (:id result))
            (assoc ::db/ready true))
    :fx [(when (some? do-after-load)
           (conj [:dispatch] do-after-load))]}))

(rf/reg-event-fx
 ::load-application
 (fn [{:keys [db]} [_ do-after-load]]
   (let [config (::db/config db)
         application-name (get config :ApplicationName)
         application-id (get config :ApplicationId)]
     (.info js/console (str "App name: " application-name))
     {:fx [[::client/call {:on-success [::application-loaded do-after-load]
                           :service    (get config :ApplicationServiceName)
                           :query      (cond

                                         application-name
                                         {:query-id :application->fetch-by-name
                                          :name (get config :ApplicationName)}

                                         application-id
                                         {:query-id :application->fetch-by-id
                                          :id (get config :ApplicationId)})}]]})))
(rf/reg-event-fx
 ::initialize-db
 (fn [{:keys [db]} [_ {:keys [selected-language
                              show-language-switcher?
                              config
                              routes
                              pages-init-events
                              translations
                              record-call-failure-func
                              record-call-func
                              on-expired-jwt-func]
                       :or   {selected-language       :en
                              show-language-switcher? false}}]]

   (let [application-name (get config :ApplicationName)
         db (-> db/default-db
                (merge db)
                (assoc-in [::db/selected-language] selected-language)
                (assoc-in [::db/show-language-switcher?] show-language-switcher?)
                (assoc ::db/config config)
                (assoc ::db/pages-init-events pages-init-events)
                (assoc ::db/routes (reitit/router routes))
                (assoc ::db/translations translations)
                (assoc ::db/record-call-failure-func record-call-failure-func)
                (assoc ::db/record-call-func record-call-func)
                (assoc ::db/on-expired-jwt-func on-expired-jwt-func))]
     {:db (cond-> db
            application-name (assoc ::db/ready false))
      :fx [(if (and (::db/user db)
                    application-name)
             [:dispatch [::load-application [::navigate
                                             (-> js/window
                                                 .-location
                                                 .-pathname)]]]
             [:dispatch [::navigate
                         (-> js/window
                             .-location
                             .-pathname)]])]})))

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
   (let [router (::db/routes db)
         pages-init-events (::db/pages-init-events db)
         new-url (if (keyword? target)
                   (-> (reitit/match-by-name router target params)
                       :path)
                   target)

         {:keys [path-params query-params data]}
         (if (keyword? target)
           {:data {:name (name target)}
            :query-patams {}
            :path-params (or params {})}
           (reitit/match-by-path router target))

         handler
         (-> data
             :name
             keyword)

         _ (.log js/console "Navigation"
                 (clj->js
                  {:params params
                   :target target
                   :route (reitit/match-by-path router target)
                   :name (reitit/match-by-name router target params)
                   :handler handler}))

         route-params
         (merge {}
                query-params
                path-params)]

     (.pushState (.-history js/window)
                 #js {}
                 ""
                 new-url)
     {:dispatch [(get pages-init-events handler)
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

(rf/reg-event-db
 :edd.events-remove-user
 (fn [db]
   (assoc-in db [::db/user] nil)))




