(ns web.widgets.translation.views
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [web.widgets.translation.subs :as subs]
    [web.widgets.translation.events :as events]
    ["@mui/material/Select" :default Select]
    ["@mui/material/MenuItem" :default MenuItem]))

(defn LangSelect [{:keys [select-props] :as props}]
  (rf/dispatch [::events/init props])
  (when @(rf/subscribe [::subs/initialized?])
    [:> Select (merge
                 {:children  (for [option @(rf/subscribe [::subs/lang-list])]
                               (r/as-element [:> MenuItem {:key option :value option} option]))
                  :value     @(rf/subscribe [::subs/selected-lang])
                  :on-change #(rf/dispatch [::events/change-lang (-> % .-target .-value)])}
                 select-props)]))