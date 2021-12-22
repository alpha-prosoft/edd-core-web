(ns web.widgets.translation.views
  (:require
   [re-frame.core :as rf]
   [web.widgets.translation.subs :as subs]
   [web.widgets.translation.events :as events]
   [web.primitives.components :refer [RawSelect]]))

(defn LangSelect [{:keys [select-props] :as props}]
  (rf/dispatch [::events/init props])
  (when @(rf/subscribe [::subs/initialized?])
    [RawSelect (merge
                {:options   @(rf/subscribe [::subs/lang-list])
                 :value     @(rf/subscribe [::subs/selected-lang])
                 :on-change #(rf/dispatch [::events/change-lang %])}
                select-props)]))