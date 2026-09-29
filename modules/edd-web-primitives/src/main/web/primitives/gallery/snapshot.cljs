(ns web.primitives.gallery.snapshot
  "El-id tooling for the gallery.

   Runs the shared el-id scan (web.primitives.testing.scan) in the browser, the
   same code the SDK's values/scan runs through Playwright, and shows the
   nested map it produces:

     take-snapshot   data: nested reading map for a CSS scope
     toggle-button   header button that opens / closes a snapshot (per example)
     snapshot-view   the printed map, taken when the button is pressed
     page-panel      the same for the whole gallery page"
  (:require
   [cljs.pprint :as pprint]
   [reagent.core :as r]
   [web.primitives.gallery.ui :as ui]
   [web.primitives.testing.scan :as scan]))

;; ---------------------------------------------------------------------------
;; Data
;; ---------------------------------------------------------------------------

(def ^:private collect-fn
  (delay ((js/Function. (str "return (" scan/collect-js ");")))))

(defn- js-entry->clj [^js entry]
  (let [id
        (aget entry 0)

        raw
        (js->clj (aget entry 1))]
    [id (scan/reading raw)]))

(defn- browser-entries [scope]
  (map js-entry->clj (@collect-fn scope)))

(defn take-snapshot
  "Nested reading map of every el-id under the `scope` CSS selector."
  [scope]
  (scan/nest (browser-entries scope)))

(defn- pretty [data]
  (with-out-str (pprint/pprint data)))

(defn- printed-snapshot [scope]
  (let [data
        (take-snapshot scope)]
    (if (empty? data)
      ";; no el-ids in this example"
      (pretty data))))

;; ---------------------------------------------------------------------------
;; Views
;; ---------------------------------------------------------------------------

(defn toggle-button
  "Header button that shows / hides the el-id snapshot held in `snapshot`
   (an atom of the printed map or nil). Taken fresh from `scope` on open."
  [{:keys [snapshot scope testid]}]
  [ui/toolbar-button {:icon     "{:}"
                      :label    (if @snapshot "Hide el-ids" "El-ids")
                      :title    "Show the el-id snapshot of this example"
                      :testid   testid
                      :active?  (some? @snapshot)
                      :on-click #(swap! snapshot (fn [shown] (when-not shown (printed-snapshot scope))))}])

(defn snapshot-view
  "The printed snapshot, or nothing while it is closed."
  [{:keys [snapshot testid]}]
  (when @snapshot
    [:div {:data-testid testid}
     ^{:key @snapshot} [ui/code-block @snapshot]]))

(defn page-panel
  "El-id snapshot of the whole gallery page, shown under the page intro."
  [_props]
  (let [snapshot (r/atom nil)]
    (fn [{:keys [scope]}]
      [:div {:data-testid "gallery-snapshot-all"
             :style       {:margin "8px 0 16px"}}
       [toggle-button {:snapshot snapshot
                       :scope    scope
                       :testid   "gallery-snapshot-all-take"}]
       [:div {:style {:margin-top "8px"}}
        [snapshot-view {:snapshot snapshot
                        :testid   "gallery-snapshot-all-result"}]]])))
