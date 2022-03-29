(ns edd.core
  (:require
   [re-frame.core :as rf]
   [edd.events :as events]

   [reagent.dom :as dom]
   [edd.i18n :as i18n]
   [pushy.core :as pushy]))

(defn- dispatch-route
  [url])

(defn mount-root
  [{:keys [body] :as ctx}]
  (pushy/start!
   (pushy/pushy #(rf/dispatch [::events/navigate %])
                (fn [url] url)))

  (dom/render
   (body ctx)
   (.getElementById js/document "app")))

(defn init
  [{:keys [translations] :as ctx}]
  (let [ctx (-> ctx
                (assoc :config (js->clj
                                (.-eddconfig js/window)
                                :keywordize-keys true))
                (merge (:config ctx {})))]
    (rf/clear-subscription-cache!)
    (rf/dispatch [::events/initialize-db ctx])
    (rf/dispatch [::events/add-translation i18n/base-translations])
    (when translations
      (rf/dispatch [::events/add-translation translations]))
    (mount-root ctx)))
