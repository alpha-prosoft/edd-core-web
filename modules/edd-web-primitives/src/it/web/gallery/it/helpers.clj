(ns web.gallery.it.helpers
  "Gallery-only scaffolding: navigation and el-id builders for the demo
   pages. All element interaction goes through web.primitives.sdk.*."
  (:require
   [testing.pw :as pw]
   [web.gallery.it.browser :as browser]
   [web.primitives.sdk.core :as sdk]
   [web.primitives.testing.elements :as els]))

;; Use the configured Chromium (ui-runtime) instead of a downloaded browser.
(browser/install!)

(defmacro with-gallery
  "Start a traced browser (clojure-playwright) and bind it for the SDK."
  [& body]
  `(pw/with-ui [page#]
     (sdk/with-page page#
       ~@body)))

(def base-url
  (or (System/getProperty "gallery.base.url")
      "http://localhost:3002"))

(defn- testid [id]
  (.locator sdk/*page* (str "[data-testid='" id "']")))

(defn- section-locator [page-id]
  (.locator sdk/*page* (str "[data-page-id='" (name page-id) "']")))

(defonce ^:private current-section (atom nil))

(defn- gallery-loaded? []
  (pos? (.count (testid "gallery-content"))))

(defn load-gallery!
  "Open the single-page gallery (every component mounted) and wait for it."
  []
  (reset! current-section nil)
  (sdk/goto! (str base-url "/"))
  (sdk/wait-visible (testid "gallery-content")))

(defn- ensure-gallery! []
  (when-not (gallery-loaded?)
    (load-gallery!)))

(defn open-page!
  "Scroll to a component section and make it the scope for section helpers."
  [page-id]
  (ensure-gallery!)
  (let [section
        (section-locator page-id)]
    (.scrollIntoViewIfNeeded section)
    (sdk/wait-visible section)
    (reset! current-section page-id)
    section))

(defn- scope ^com.microsoft.playwright.Locator []
  (section-locator @current-section))

(defn- scoped-testid [id]
  (.locator (scope) (str "[data-testid='" id "']")))

(defn- scoped-testid-prefix [prefix]
  (.locator (scope) (str "[data-testid^='" prefix "']")))

(defn el
  "el-id of a demo element on gallery page `page-id`."
  [page-id variant]
  (els/el page-id variant))

(defn readout
  "el-id of the state readout printed under an interactive demo."
  [page-id variant]
  (els/el page-id variant :readout))

;; ---------------------------------------------------------------------------
;; Gallery chrome (sidebar, code toggles, props panel). These are gallery UI,
;; not primitives, so they live here instead of in the SDK.
;; ---------------------------------------------------------------------------

(defn page-ids
  "Ids of every component section on the gallery page, in order."
  []
  (ensure-gallery!)
  (let [sections
        (testid "gallery-section")]
    (vec
     (for [i (range (.count sections))]
       (.getAttribute (.nth sections i) "data-page-id")))))

(defn sidebar-text []
  (.textContent (testid "gallery-sidebar")))

(defn section-in-viewport? [page-id]
  (.evaluate (section-locator page-id)
             "el => { const r = el.getBoundingClientRect();
                      return r.top < window.innerHeight && r.bottom > 0; }"))

(defn click-sidebar-link!
  "Click a sidebar link and wait until its section is scrolled into view."
  [page-id]
  (.click (testid (str "gallery-link-" (name page-id))))
  (sdk/eventually #(section-in-viewport? page-id))
  (reset! current-section page-id))

(defn page-title []
  (.innerText (.locator (scope) "h2")))

(defn has-doc? []
  (= 1 (.count (scoped-testid "gallery-page-doc"))))

(defn has-props-panel? []
  (= 1 (.count (scoped-testid "gallery-props"))))

(defn variant-ids
  "Variant ids shown in the current section."
  []
  (let [bodies
        (scoped-testid-prefix "variant-body-")]
    (vec
     (for [i (range (.count bodies))]
       (subs (.getAttribute (.nth bodies i) "data-testid")
             (count "variant-body-"))))))

(defn- code-block [variant]
  (.locator (scoped-testid (str "variant-code-" (name variant))) "code"))

(defn- code-toggle [variant]
  (scoped-testid (str "variant-code-toggle-" (name variant))))

(defn code-toggle? [variant]
  (= 1 (.count (code-toggle variant))))

(defn code-visible? [variant]
  (pos? (.count (code-block variant))))

(defn toggle-code!
  "Click the code toggle and wait until visibility actually flips, so a
   read immediately after does not race the show/hide re-render."
  [variant]
  (let [before
        (code-visible? variant)]
    (.click (code-toggle variant))
    (sdk/eventually #(not= before (code-visible? variant)))))

(defn show-code!
  "Open the code block of `variant` and return its text."
  [variant]
  (when-not (code-visible? variant)
    (toggle-code! variant))
  (sdk/wait-visible (code-block variant))
  (.textContent (code-block variant)))

(defn code-highlighted? [variant]
  (pos? (.count (.locator (code-block variant) "span[class^='hljs-']"))))

(defn open-code-blocks
  "Number of variant code blocks currently open in the current section."
  []
  (.count (.locator (scope) "[data-testid^='variant-code-'] pre code")))

(defn- props-rows []
  (.locator (scoped-testid "gallery-props") "tbody tr"))

(defn props-row-count []
  (.count (props-rows)))

(defn expand-props! []
  (.click (scoped-testid "gallery-props-toggle"))
  (sdk/wait-visible (.first (props-rows))))

(defn props-text []
  (.textContent (scoped-testid "gallery-props")))

(defn show-snapshot!
  "Open the El-ids snapshot of `variant` in the current section and return
   the printed map."
  [variant]
  (.click (scoped-testid (str "variant-el-ids-toggle-" (name variant))))
  (let [result
        (.locator (scoped-testid (str "variant-el-ids-" (name variant))) "code")]
    (sdk/wait-visible result)
    (.textContent result)))
