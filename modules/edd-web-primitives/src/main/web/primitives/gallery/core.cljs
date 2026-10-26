(ns web.primitives.gallery.core
  "Gallery: an embeddable catalogue of registered component pages.

   Every registered page is rendered as a section of ONE page, so loading the
   gallery mounts every component at once and any render error surfaces
   immediately. The sidebar only scrolls to a section; it never swaps content.

   Pages are added with web.primitives.gallery.page/register!, usually from
   the :register! fn passed to Gallery. PrimitivesGallery is the catalogue of
   the web primitives built on top of it."
  (:require
   [clojure.string :as str]
   [reagent.core :as r]
   [web.primitives.gallery.registry :as registry]
   [web.primitives.gallery.snapshot :as snapshot]
   [web.primitives.gallery.ui :as ui]
   [web.primitives.utils :refer [apply-stiles]]))

;; ---------------------------------------------------------------------------
;; Sections and scrolling
;; ---------------------------------------------------------------------------

(def ^:private default-group-order
  ["Inputs" "Data display" "Feedback" "Surfaces" "Navigation" "Layout" "Lab" "Utils" "Other" "Examples"])

(defn- page-group [p]
  (or (:group p) "Other"))

(defn- pages-by-group [group-order]
  (let [grouped
        (group-by page-group (registry/all-pages))]
    (for [g group-order
          :let [ps (get grouped g)]
          :when (seq ps)]
      [g (sort-by :title ps)])))

(defn- ordered-pages [group-order]
  (mapcat second (pages-by-group group-order)))

(defn- section-selector [page-id]
  (str "[data-page-id='" (name page-id) "']"))

(defn- hash->page-id [hash]
  (let [without-prefix
        (if (and hash (str/starts-with? hash "#/"))
          (subs hash 2)
          "")]
    (when (seq without-prefix)
      (keyword without-prefix))))

(defn- remember-in-url! [page-id]
  (.replaceState js/history nil "" (str "#/" (name page-id))))

(defn- scroll-to-section! [page-id]
  (when-let [node (.querySelector js/document (section-selector page-id))]
    (.scrollIntoView node #js {:block "start"})))

(defn- scroll-to-url-section! []
  (when-let [page-id (hash->page-id (.. js/window -location -hash))]
    (scroll-to-section! page-id)))

;; ---------------------------------------------------------------------------
;; Sidebar navigation (scroll only)
;; ---------------------------------------------------------------------------

(defn- nav-item [{:keys [id title]} active? on-select]
  [:a {:href        (str "#/" (name id))
       :data-testid (str "gallery-link-" (name id))
       :on-click    (fn [^js e]
                      (.preventDefault e)
                      (on-select id))
       :style       {:display         "block"
                     :padding         "6px 12px"
                     :border-radius   "6px"
                     :text-decoration "none"
                     :font-size       "14px"
                     :color           (if active? "#0f766e" "#374151")
                     :background      (when active? "#ecfdf5")
                     :font-weight     (if active? 600 400)}}
   (or title (name id))])

(defn- sidebar [{:keys [active-id title group-order on-select]}]
  [:nav {:data-testid "gallery-sidebar"
         :style       {:width        "240px"
                       :flex-shrink  0
                       :height       "100vh"
                       :overflow-y   "auto"
                       :border-right "1px solid #e5e7eb"
                       :padding      "16px 12px"
                       :box-sizing   "border-box"
                       :position     "sticky"
                       :top          0}}
   [:div {:style {:font-size "18px" :font-weight 700 :color "#111827" :padding "4px 12px 16px"}}
    title]
   (for [[group ps] (pages-by-group group-order)]
     [:div {:key group :style {:margin-bottom "16px"}}
      [:div {:style {:font-size "11px" :text-transform "uppercase" :letter-spacing "0.05em"
                     :color "#9ca3af" :padding "4px 12px" :font-weight 600}}
       group]
      (for [p ps]
        ^{:key (str (:id p))}
        [nav-item p (= (:id p) active-id) on-select])])])

;; ---------------------------------------------------------------------------
;; Example card (live demo + Code and El-ids toggles)
;; ---------------------------------------------------------------------------

(defn- variant-testid [prefix id]
  (str prefix (name id)))

(defn- body-selector [page-id variant-id]
  (str (section-selector page-id)
       " [data-testid='" (variant-testid "variant-body-" variant-id) "']"))

(defn- code-toggle-button [id show-code?]
  [ui/toolbar-button {:icon     "</>"
                      :label    (if @show-code? "Hide code" "Code")
                      :title    "Show code"
                      :testid   (variant-testid "variant-code-toggle-" id)
                      :active?  @show-code?
                      :on-click #(swap! show-code? not)}])

(defn- card-header [{:keys [id title code]} page-id show-code? snapshot]
  [:div {:style {:display "flex" :align-items "center" :justify-content "space-between"
                 :padding "10px 16px" :border-bottom "1px solid #f3f4f6" :background "#fafafa"}}
   [:h3 {:style {:margin 0 :font-size "15px" :font-weight 600}} (or title (name id))]
   [:div {:style {:display "flex" :gap "8px"}}
    [snapshot/toggle-button {:snapshot snapshot
                             :scope    (body-selector page-id id)
                             :testid   (variant-testid "variant-el-ids-toggle-" id)}]
    (when code
      [code-toggle-button id show-code?])]])

(defn- example-card [_example _page-id]
  (let [show-code? (r/atom false)
        snapshot   (r/atom nil)]
    (fn [{:keys [id code render] :as example} page-id]
      [:section {:data-testid (variant-testid "variant-" id)
                 :style       {:margin-bottom "24px"
                               :border        "1px solid #e5e7eb"
                               :border-radius "10px"
                               :overflow      "hidden"}}
       [card-header example page-id show-code? snapshot]
       [:div {:data-testid (variant-testid "variant-body-" id)
              :style       {:padding "20px 16px"}}
        (apply-stiles [render])]
       [:div {:style {:border-top (when @snapshot "1px solid #f3f4f6")}}
        [snapshot/snapshot-view {:snapshot snapshot
                                 :testid   (variant-testid "variant-el-ids-" id)}]]
       (when (and code @show-code?)
         [:div {:data-testid (variant-testid "variant-code-" id)
                :style       {:border-top "1px solid #f3f4f6"}}
          [ui/code-block code]])])))

;; ---------------------------------------------------------------------------
;; Component section
;; ---------------------------------------------------------------------------

(defn- section-header [{:keys [title doc]}]
  [:div {:style {:margin-bottom "24px"}}
   [:h2 {:style {:margin "0 0 8px" :font-size "26px"}} title]
   (when doc
     [:p {:data-testid "gallery-page-doc"
          :style       {:color "#4b5563" :font-size "15px" :line-height "1.6" :margin 0}}
      doc])])

(defn- props-section [{:keys [props]}]
  (when (seq props)
    [ui/collapsible {:label         (str "Props (" (count props) ")")
                     :testid        "gallery-props"
                     :default-open? (<= (count props) 6)}
     [ui/props-table props]]))

(defn- component-section [{:keys [id variants] :as page}]
  [:section {:data-testid  "gallery-section"
             :data-page-id (name id)
             :style        {:padding       "32px 0"
                            :border-bottom "1px solid #e5e7eb"
                            :scroll-margin-top "16px"}}
   [section-header page]
   [props-section page]
   [:div {:style {:margin-top "24px"}}
    (for [v variants]
      ^{:key (str (:id v))}
      [example-card v id])]])

(defn- all-sections [{:keys [heading intro group-order]}]
  [:main {:data-testid "gallery-content"
          :style       {:flex 1 :padding "0 40px" :max-width "900px"}}
   [:h1 {:style {:font-size "32px" :margin "32px 0 8px"}} heading]
   (when intro
     [:p {:style {:color "#4b5563" :font-size "16px"}}
      intro])
   [snapshot/page-panel {:scope "[data-testid='gallery-content']"}]
   (for [p (ordered-pages group-order)]
     ^{:key (str (:id p))}
     [component-section p])])

;; ---------------------------------------------------------------------------
;; Gallery
;; ---------------------------------------------------------------------------

(defn Gallery
  "Single-page catalogue of every registered gallery page.

   Optional props:
     :register!   fn called once on mount to register the pages to show
     :title       sidebar heading (default \"Components\")
     :heading     content heading (default \"Component browser\")
     :intro       text under the heading
     :group-order vector of group names controlling section order

   A #/page-id hash on load scrolls to that section; clicking the sidebar
   scrolls and updates the hash without re-rendering."
  [{:keys [register!] :as props}]
  (r/with-let [_pages
               (when register!
                 (register!))

               active-id
               (r/atom (hash->page-id (.. js/window -location -hash)))

               select!
               (fn [page-id]
                 (reset! active-id page-id)
                 (remember-in-url! page-id)
                 (scroll-to-section! page-id))

               _initial-scroll
               (js/requestAnimationFrame scroll-to-url-section!)]
    (let [{:keys [title heading intro group-order]
           :or   {title       "Components"
                  heading     "Component browser"
                  group-order default-group-order}}
          props]
      [:div {:data-testid "gallery-root"
             :style       {:display "flex" :min-height "100vh"
                           :font-family "'Inter', system-ui, sans-serif" :color "#111827"}}
       [sidebar {:active-id   @active-id
                 :title       title
                 :group-order group-order
                 :on-select   select!}]
       [all-sections {:heading     heading
                      :intro       intro
                      :group-order group-order}]])))
