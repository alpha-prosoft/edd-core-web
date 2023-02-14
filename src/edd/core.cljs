(ns edd.core
  (:require
   [re-frame.core :as rf]
   [edd.events :as events]

   [reagent.dom :as dom]
   [edd.i18n :as i18n]
   [pushy.core :as pushy]))

(defn mount-root
  [{:keys [body] :as ctx}]
  (pushy/start!
   (pushy/pushy #(rf/dispatch [::events/navigate %])
                (fn [url] url)))

  (dom/render
   (body ctx)
   (.getElementById js/document "app")))

(defn init
  [{:keys [translations
           pages
           panels] :as ctx}]
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
        config  (merge (js->clj
                        (.-eddconfig js/window)
                        :keywordize-keys true)
                       (:config ctx {}))
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
                                                       :pages-init-events])])
    (doseq [widget-init (:widgets ctx [])]
      (apply widget-init [ctx]))
    (mount-root ctx)))
