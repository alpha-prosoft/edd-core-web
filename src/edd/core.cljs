(ns edd.core
  (:require
   ["react" :refer [StrictMode]]
   [edd.events :as events]
   [edd.subs :as subs]
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
     [:vector keyword?]]
    [:placeholder
     {:optional true}
     fn?]]))

(defn ^:export activate-request-feature [feature-key-name feature-value]
  (let [feature-key (keyword feature-key-name)]
    (rf/dispatch [::events/activate-request-feature feature-key feature-value])))

(defn ^:export deactivate-request-feature
  ([] (rf/dispatch [::events/deactivate-request-feature]))
  ([feature-key] (rf/dispatch [::events/deactivate-request-feature (keyword feature-key)])))

(defn ^:export print-features []
  (let [request-features @(rf/subscribe [::subs/request-features])]
    (if (some? request-features)
      (cljs.pprint/pprint request-features)
      (print (str "Request-features were not set")))))

(defn init
  [{:keys [translations
           pages
           panels
           config]
    :or {config {}}
    :as ctx}]
  (when-not (m/validate CtxSchema ctx)
    (let [explanation
          (m/explain CtxSchema ctx)

          humanized
          (me/humanize explanation)

          json-str
          (.stringify js/JSON (clj->js humanized))]
      (throw (js/Error. (str "Ctx does not match schema: " json-str)))))
  (let [ctx
        (dissoc ctx :panels)

        pages
        (or pages
            (reduce
             (fn [p [key panel]]
               (assoc p key
                      {:init  (keyword (str "initialize-" (name key) "-db"))
                       :panel panel}))
             {}
             panels))

        pages
        (reduce
         (fn [p [key val]]
           (assoc p key (apply val [ctx])))
         {}
         pages)

        pages-init-events
        (reduce
         (fn [p [key {:keys [init]}]]
           (assoc p key init))
         {}
         pages)

        pages-url-params
        (reduce
         (fn [p [key {:keys [url-params]}]]
           (if url-params
             (assoc p key (m/schema url-params))
             p))
         {}
         pages)

        error-pages
        (or (:error-pages ctx) {})

        edd-config
        (json/parse-custom-fields
         (js->clj (.-eddconfig js/window) :keywordize-keys true))

        config
        (merge edd-config config)

        translations
        (cond-> i18n/base-translations
          translations (i18n/deep-merge translations))

        ctx
        (assoc ctx
               :translations translations
               :pages-init-events pages-init-events
               :pages-url-params pages-url-params
               :error-pages error-pages
               :config config
               :pages pages)]

    (rf/clear-subscription-cache!)
    (rf/dispatch [::events/initialize-db (select-keys ctx
                                                      [:selected-language
                                                       :show-language-switcher?
                                                       :config
                                                       :routes
                                                       :pages-init-events
                                                       :pages-url-params
                                                       :error-pages
                                                       :translations
                                                       :record-call-failure-func
                                                       :record-call-func
                                                       :on-expired-jwt-func])])
    (doseq [widget-init (:widgets ctx [])]
      (apply widget-init [ctx]))
    (mount-root ctx)))
