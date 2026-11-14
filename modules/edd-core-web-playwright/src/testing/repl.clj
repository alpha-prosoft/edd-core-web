(ns testing.repl
  "nREPL helper for interactive Playwright browser exploration.

   Start with:
     clj -M:nrepl

   Then connect your editor to port 7888 (or whatever is printed).

   Quick reference:
     (require '[testing.repl :refer [page ctx restart stop]])

     (.navigate @page \"https://example.com\")
     (.textContent (.locator @page \"h1\"))
     (.screenshot @page)
     (.title @page)

     (restart)   ;; fresh browser
     (stop)      ;; close browser
  "
  (:require
   [nrepl.server :as nrepl]
   [testing.pw :as pw]))

(defonce ^:private browser-map (atom nil))

(def page
  "Atom holding the current Playwright Page.  Deref with @page."
  (atom nil))

(def ctx
  "Atom holding the current BrowserContext.  Deref with @ctx."
  (atom nil))

;; ---------------------------------------------------------------------------
;; Lifecycle
;; ---------------------------------------------------------------------------

(defn stop
  "Close the browser (if running)."
  []
  (when @browser-map
    (try (pw/stop-browser @browser-map) (catch Exception _))
    (reset! browser-map nil)
    (reset! page nil)
    (reset! ctx nil)
    (println "Browser stopped.")))

(defn- headless?
  []
  (= "true" (System/getProperty "playwright.browser.headless" "false")))

(defn restart
  "Close any existing browser and launch a fresh Chromium, headed unless
   -Dplaywright.browser.headless=true. Returns the Page object."
  []
  (stop)
  (let [bm (pw/start-browser {:headless (headless?)})]
    (reset! browser-map bm)
    (reset! page (:page bm))
    (reset! ctx (:context bm))
    (println "Browser started. Use @page to interact.")
    @page))

;; ---------------------------------------------------------------------------
;; nREPL entry point
;; ---------------------------------------------------------------------------

(defn- try-load-cider
  "Load cider-nrepl middleware if available, fall back to default handler."
  []
  (try
    (require 'cider.nrepl)
    (let [h (resolve 'cider.nrepl/cider-nrepl-handler)]
      (if h
        (do (println "  cider-nrepl loaded.") @h)
        (nrepl/default-handler)))
    (catch Exception _
      (println "  cider-nrepl not found, using default handler.")
      (nrepl/default-handler))))

(defn -main
  "Start nREPL server and a browser for interactive exploration.
   Args: [port & namespaces]; the namespaces are required once the browser
   is up, e.g. an app's explore helpers."
  [& args]
  (let [port    (if (seq args) (Integer/parseInt (first args)) 7888)
        handler (try-load-cider)
        _server (nrepl/start-server :port port :handler handler)]
    (spit ".nrepl-port" (str port))
    (println)
    (println "============================================")
    (println " Playwright Clojure — Interactive REPL")
    (println "============================================")
    (println (str "  nREPL on port " port))
    (println "  Starting browser...")
    (println)
    (restart)
    (doseq [ns-name (rest args)]
      (println "  requiring" ns-name)
      (require (symbol ns-name)))
    (println)
    (println "  (require '[testing.repl :refer [page ctx restart stop]])")
    (println "  (.navigate @page \"https://example.com\")")
    (println "  (.textContent (.locator @page \"h1\"))")
    (println "  (restart) / (stop)")
    (println "============================================")
    @(promise)))
