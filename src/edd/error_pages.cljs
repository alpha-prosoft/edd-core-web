(ns edd.error-pages
  (:require [edd.i18n :refer [tr]]))

(def ^:private styles
  {:container  {:display         "flex"
                :flex-direction  "column"
                :align-items     "center"
                :justify-content "center"
                :min-height      "60vh"
                :font-family     "system-ui, -apple-system, sans-serif"
                :color           "#333"
                :padding         "2rem"
                :text-align      "center"}
   :code       {:font-size   "8rem"
                :font-weight "700"
                :margin      "0"
                :line-height "1"
                :color       "#e0e0e0"}
   :title      {:font-size   "1.5rem"
                :font-weight "600"
                :margin      "0.5rem 0"}
   :message    {:font-size   "1rem"
                :color       "#888"
                :margin      "0.5rem 0 2rem"
                :max-width   "28rem"
                :line-height "1.5"}
   :link       {:display         "inline-block"
                :padding         "0.6rem 1.5rem"
                :border-radius   "4px"
                :background      "#1976d2"
                :color           "#fff"
                :text-decoration "none"
                :font-size       "0.875rem"
                :cursor          "pointer"
                :border          "none"
                :transition      "background 0.2s"}
   :detail     {:font-size   "0.75rem"
                :color       "#bbb"
                :margin-top  "2rem"
                :font-family "monospace"
                :max-width   "32rem"
                :word-break  "break-all"}})

(defn not-found-page
  []
  [:div {:style (:container styles)}
   [:p {:style (:code styles)} "404"]
   [:h1 {:style (:title styles)} (tr :error :not-found-title)]
   [:p {:style (:message styles)} (tr :error :not-found-message)]
   [:a {:href "/"
        :style (:link styles)} (tr :error :go-home)]])

(defn bad-request-page
  []
  [:div {:style (:container styles)}
   [:p {:style (:code styles)} "400"]
   [:h1 {:style (:title styles)} (tr :error :bad-request-title)]
   [:p {:style (:message styles)} (tr :error :bad-request-message)]
   [:a {:href "/"
        :style (:link styles)} (tr :error :go-home)]])
