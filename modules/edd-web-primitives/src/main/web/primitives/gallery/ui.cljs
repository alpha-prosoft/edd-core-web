(ns web.primitives.gallery.ui
  "Presentational helpers for the gallery: syntax-highlighted code blocks,
   collapsible sections and prop tables."
  (:require
   [reagent.core :as r]
   ["highlight.js/lib/core" :default hljs]
   ["highlight.js/lib/languages/clojure" :as clojure-language]))

(.registerLanguage hljs "clojure" clojure-language)

(defn- highlighted-html [code]
  (.-value (.highlight hljs code #js {:language "clojure" :ignoreIllegals true})))

(defn- highlight-into! [code ^js node]
  (when node
    (set! (.-innerHTML node) (highlighted-html code))))

(def ^:private pre-style
  {:margin        0
   :padding       "14px 16px"
   :background    "#0d1117"
   :color         "#c9d1d9"
   :border-radius "8px"
   :overflow-x    "auto"
   :font-size     "13px"
   :line-height   "1.5"
   :white-space   "pre"
   :font-family   "'JetBrains Mono', 'Fira Code', monospace"})

(defn code-block
  "Read-only, syntax-highlighted Clojure code block. The highlighted HTML is
   written straight into the node on mount; the plain code is the fallback."
  [code]
  [:pre {:style pre-style}
   [:code {:class "hljs language-clojure"
           :ref   (partial highlight-into! code)
           :style {:background "transparent" :padding 0}}
    code]])

(defn toolbar-button
  "Small toggle button used in example-card headers (Code, El-ids).
   `icon` is a short monospace glyph; `active?` highlights the button."
  [{:keys [icon label title testid active? on-click]}]
  [:button {:on-click    on-click
            :data-testid testid
            :title       title
            :style       {:border        "1px solid #d1d5db"
                          :background    (if active? "#ecfdf5" "#fff")
                          :border-radius "6px"
                          :cursor        "pointer"
                          :padding       "4px 8px"
                          :font-size     "12px"
                          :display       "flex"
                          :align-items   "center"
                          :gap           "6px"}}
   [:span {:style {:font-family "monospace" :font-weight 700}} icon]
   label])

(defn collapsible
  "A section that starts collapsed and expands on click.
   `label` is the always-visible header; `body` renders when open."
  [{:keys [default-open?]} & _body]
  (let [open? (r/atom (boolean default-open?))]
    (fn [{:keys [label testid]} & body]
      [:div {:data-testid testid
             :style       {:border        "1px solid #e5e7eb"
                           :border-radius "8px"
                           :overflow      "hidden"
                           :margin        "8px 0"}}
       [:button {:on-click #(swap! open? not)
                 :data-testid (when testid (str testid "-toggle"))
                 :style    {:width       "100%"
                            :text-align  "left"
                            :padding     "10px 14px"
                            :background  "#f9fafb"
                            :border      "none"
                            :cursor      "pointer"
                            :font-weight 600
                            :font-size   "14px"
                            :display     "flex"
                            :align-items "center"
                            :gap         "8px"}}
        [:span {:style {:transform (when @open? "rotate(90deg)")
                        :transition "transform .15s"
                        :display "inline-block"}} "▶"]
        label]
       (when @open?
         (into [:div {:style {:padding "12px 14px"}}] body))])))

(defn props-table
  "Render a component's prop documentation.
   `props` is a vector of {:name :type :default :doc}."
  [props]
  [:table {:style {:width "100%" :border-collapse "collapse" :font-size "14px"}}
   [:thead
    [:tr {:style {:text-align "left" :border-bottom "2px solid #e5e7eb"}}
     [:th {:style {:padding "8px"}} "Prop"]
     [:th {:style {:padding "8px"}} "Type"]
     [:th {:style {:padding "8px"}} "Default"]
     [:th {:style {:padding "8px"}} "Description"]]]
   (into [:tbody]
         (for [{:keys [name type default doc]} props]
           [:tr {:key (str name) :style {:border-bottom "1px solid #f3f4f6" :vertical-align "top"}}
            [:td {:style {:padding "8px"}}
             [:code {:style {:background "#f3f4f6" :padding "2px 6px" :border-radius "4px"
                             :font-family "monospace" :color "#0f766e"}}
              (str name)]]
            [:td {:style {:padding "8px" :color "#6b7280" :font-family "monospace"}} (or type "")]
            [:td {:style {:padding "8px" :color "#6b7280" :font-family "monospace"}} (or default "—")]
            [:td {:style {:padding "8px"}} (or doc "")]]))])
