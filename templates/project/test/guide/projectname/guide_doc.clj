(ns projectname.guide-doc
  "Block DSL for the user guides. A guide is code that drives the running app
   and adds text and screenshots; `run-guide!` renders them to
   docs/guide/<file> with images in docs/guide/img."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [testing.pw :as pw]
            [web.primitives.sdk.core :as sdk])
  (:import [com.microsoft.playwright Locator$ScreenshotOptions Page$ScreenshotOptions]
           [com.microsoft.playwright.options Clip]))

(def site-dir
  "docs/guide")

(def device-scale
  2)

(def ^:dynamic *blocks* nil)

(defn- add!
  [block]
  (swap! *blocks* conj block)
  nil)

(defn chapter!
  [title & [intro]]
  (add! {:type :chapter :title title :intro intro}))

(defn p!
  [text]
  (add! {:type :p :text text}))

(defn steps!
  [& items]
  (add! {:type :steps :items items}))

(defn bullets!
  [& items]
  (add! {:type :bullets :items items}))

(defn tip!
  [text]
  (add! {:type :tip :text text}))

(def colors
  {:red    "#d32f2f"
   :blue   "#1565c0"
   :green  "#2e7d32"
   :orange "#e65100"})

(def ^:private annotate-js
  "([targets]) => {
     document.querySelectorAll('.guide-mark').forEach(n => n.remove());
     const style = document.createElement('style');
     style.className = 'guide-mark';
     style.textContent = '*{caret-color:transparent !important;animation:none !important;transition:none !important}' +
                         '.MuiTouchRipple-root{display:none !important}';
     document.head.appendChild(style);
     const extent = {left: Infinity, top: Infinity, right: -Infinity, bottom: -Infinity};
     const grow = r => {
       extent.left = Math.min(extent.left, r.left); extent.top = Math.min(extent.top, r.top);
       extent.right = Math.max(extent.right, r.right); extent.bottom = Math.max(extent.bottom, r.bottom);
     };
     targets.forEach(t => {
       const el = document.querySelector(t.selector);
       if (!el) return;
       const r = el.getBoundingClientRect();
       const box = document.createElement('div');
       box.className = 'guide-mark';
       Object.assign(box.style, {position: 'fixed', zIndex: 2147483647, pointerEvents: 'none',
                                 left: (r.left - 5) + 'px', top: (r.top - 5) + 'px',
                                 width: (r.width + 10) + 'px', height: (r.height + 10) + 'px',
                                 border: '3px solid ' + t.color, borderRadius: '8px'});
       document.body.appendChild(box);
       const label = document.createElement('div');
       label.className = 'guide-mark';
       label.textContent = t.label;
       Object.assign(label.style, {position: 'fixed', zIndex: 2147483647, pointerEvents: 'none',
                                   left: (r.right + 14) + 'px', top: r.top + 'px',
                                   background: t.color, color: '#fff', padding: '4px 8px', borderRadius: '6px',
                                   font: '600 13px/1.3 system-ui, sans-serif', whiteSpace: 'nowrap'});
       document.body.appendChild(label);
       grow(box.getBoundingClientRect());
       grow(label.getBoundingClientRect());
     });
     return extent;
   }")

(def crop-padding
  40)

(defn- clip
  [extent]
  (let [viewport
        (.viewportSize (sdk/page))

        left
        (max 0 (- (get extent "left") crop-padding))

        top
        (max 0 (- (get extent "top") crop-padding))

        right
        (min (.-width viewport) (+ (get extent "right") crop-padding))

        bottom
        (min (.-height viewport) (+ (get extent "bottom") crop-padding))]
    (Clip. (double left) (double top) (double (- right left)) (double (- bottom top)))))

(defn- shoot!
  [img-name {:keys [ctx marks]}]
  (let [targets
        (mapv (fn [{:keys [target label color]}]
                {"selector" (sdk/by-el-id target)
                 "label"    label
                 "color"    (get colors (or color :red))})
              marks)

        path
        (.toPath (io/file site-dir "img" (str img-name ".png")))]
    (io/make-parents (.toFile path))
    (let [extent
          (.evaluate (sdk/page) annotate-js [targets])]
      (cond
        ctx
        (.screenshot (sdk/el ctx) (.setPath (Locator$ScreenshotOptions.) path))

        (seq marks)
        (.screenshot (sdk/page) (.setClip (.setPath (Page$ScreenshotOptions.) path) (clip extent)))

        :else
        (.screenshot (sdk/page) (.setPath (Page$ScreenshotOptions.) path))))
    (.evaluate (sdk/page) "() => document.querySelectorAll('.guide-mark').forEach(n => n.remove())")))

(defn figure!
  "Screenshots the area around `:marks` [{:target el-id :label \"...\" :color :blue}],
   outlined and labelled, or the element `:ctx` when given."
  [img-name caption opts]
  (shoot! img-name opts)
  (add! {:type :figure :img img-name :caption caption}))

(defn- render-block
  [{:keys [type title intro text items img caption]}]
  (case type
    :chapter (str "## " title (when intro (str "\n\n" intro)))
    :p text
    :steps (string/join "\n" (map-indexed (fn [idx item] (str (inc idx) ". " item)) items))
    :bullets (string/join "\n" (map #(str "- " %) items))
    :tip (str "> **Tip:** " text)
    :figure (str "![" (string/replace caption #"\*\*" "") "](img/" img ".png)\n\n"
                 "*" caption "*")))

(defn- markdown
  [{:keys [title lead]} blocks]
  (str "# " title "\n\n"
       (when lead (str lead "\n\n"))
       (string/join "\n\n" (map render-block blocks))
       "\n"))

(defn start-browser
  []
  (pw/start-browser {:test-name           "guide"
                     :viewport            [1280 800]
                     :device-scale-factor device-scale
                     :color-scheme        :light}))

(defn stop-browser
  [browser ok?]
  (pw/stop-browser browser (if ok? :pass :fail)))

(defn run-guide!
  "Runs `body-fn` and writes the guide only when it completed, so a failure
   keeps the published page. True when written."
  [{:keys [title file] :as guide} body-fn]
  (println "== Guide:" title)
  (let [blocks
        (atom [])]
    (try
      (binding [*blocks* blocks]
        (body-fn))
      (io/make-parents (io/file site-dir file))
      (spit (io/file site-dir file) (markdown guide @blocks))
      (println "   wrote" (str site-dir "/" file))
      true
      (catch Throwable t
        (println "   FAILED:" (ex-message t))
        false))))
