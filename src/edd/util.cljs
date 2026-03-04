(ns edd.util
  (:require
   [re-frame.core :as rf]
   [edd.subs :as subs]
   [edd.error-pages :as error-pages]))

(defn placeholder
  [{:keys [pages classes] :as _ctx}]
  (let [active-panel
        @(rf/subscribe [::subs/active-panel])

        error
        @(rf/subscribe [::subs/error])

        error-overrides
        @(rf/subscribe [::subs/error-pages])]
    (cond
      (= active-panel :edd/not-found)
      (if-let [custom (:not-found error-overrides)]
        [custom error]
        [error-pages/not-found-page])

      (= active-panel :edd/bad-request)
      (if-let [custom (:bad-request error-overrides)]
        [custom error]
        [error-pages/bad-request-page])

      (contains? pages active-panel)
      [(get-in pages [active-panel :panel]) classes]

      :else
      [error-pages/not-found-page])))
