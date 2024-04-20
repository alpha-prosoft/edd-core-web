(ns edd.core
  (:require
   ["react" :refer [StrictMode]]
   [edd.events :as events]
   [edd.i18n :as i18n]
   [edd.json :as json]
   [malli.core :as m]
   [malli.error :as me]
   [re-frame.core :as rf]
   [reagent.dom.client :as dom]))

(defonce root (dom/create-root
               (.getElementById js/document "app")))
(defn mount-root
  [{:keys [body] :as ctx}]
  (dom/render
   root
   [:> StrictMode
    (body ctx)]))

(def CtxSchema
  (m/schema
   [:map
    [:selected-language
     {:optional true}
     keyword?]
    [:show-language-switcher?
     {:optional true}
     boolean?]
    [:config
     {:optional true}
     [:map]]
    [:routes
     [:vector :any]]
    [:languages
     [:vector keyword?]]]))

(defn init
  [{:keys [translations
           pages
           panels
           config]
    :or {config {}}
    :as ctx}]
  (when-not (m/validate CtxSchema ctx)
    (throw (js/Error. (str "Ctx does not match schema: "
                           (->> (m/explain CtxSchema ctx)
                                me/humanize
                                clj->js
                                (.stringify js/JSON))))))
  (let [ctx (dissoc ctx :panels)
        pages (or pages
                  (reduce
                   (fn [p [key panel]]
                     (assoc p key
                            {:init  (keyword (str "initialize-" (name key) "-db"))
                             :panel panel}))
                   {}
                   panels))
        pages (reduce
               (fn [p [key val]]
                 (assoc p key (apply val [ctx])))
               {}
               pages)
        pages-init-events (reduce
                           (fn [p [key {:keys [init]}]]
                             (assoc p key init))
                           {}
                           pages)
        config  (merge (-> (js->clj
                            (.-eddconfig js/window)
                            :keywordize-keys true)
                           json/parse-custom-fields)
                       config)
        translations (cond-> i18n/base-translations
                       translations (merge translations))
        ctx (assoc ctx
                   :translations translations
                   :pages-init-events pages-init-events
                   :config config
                   :pages pages)]

    (rf/clear-subscription-cache!)
    (rf/dispatch [::events/initialize-db (select-keys ctx
                                                      [:selected-language
                                                       :show-language-switcher?
                                                       :config
                                                       :routes
                                                       :pages-init-events
                                                       :translations
                                                       :record-call-failure-func
                                                       :record-call-func
                                                       :on-expired-jwt-func])])
    (doseq [widget-init (:widgets ctx [])]
      (apply widget-init [ctx]))
    (mount-root ctx)))
