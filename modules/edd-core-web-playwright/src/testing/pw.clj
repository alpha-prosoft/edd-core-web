(ns testing.pw
  "Playwright-native testing library for Clojure.

   Provides:
   - `with-ui`       — main test macro; manages browser + tracing lifecycle.
                        Produces a trace.zip per test, viewable in Playwright
                        Trace Viewer (npx playwright show-trace / trace.playwright.dev).
                        Binds `*current-page*` dynamic var for the duration of the test.
                        (`with-pw` is a deprecated alias for `with-ui`.)
   - `with-api`      — macro that provides an APIRequestContext bound to `client`.
                        If `*current-page*` is already bound (i.e. inside `with-pw`),
                        derives the client from the existing traced BrowserContext so
                        HTTP calls appear in the trace.  Otherwise starts a lightweight
                        headless browser+context with tracing and tears it down after body.
   - `step`          — annotate a logical step in the trace (group of actions).
                        Reads page from `*current-page*`; no explicit context needed.
   - `pw-screenshot` — manually attach a labelled screenshot to the trace.
                        Reads page from `*current-page*`; no explicit page arg needed.

   Design goals
   ============
   * Zero JUnit dependency — pure Playwright Java API on the JVM.
   * Every test automatically produces target/pw-traces/<test-name>-<ts>.zip
   * Traces are enriched with custom step annotations via
     BrowserContext.tracing().group() / groupEnd(), which appear as named
     groups in the Trace Viewer action list.
   * A separate `testing.pw-report` namespace can aggregate all zips into
     a static HTML dashboard.

   Usage
   =====
     (ns my.tests
       (:require [clojure.test :refer [deftest is]]
                 [testing.pw  :refer [pw-screenshot step with-ui with-api]]))

     (deftest homepage-test
       (with-ui [page]
         (step \"Navigate to example.com\"
           (fn []
             (.navigate page \"https://example.com\")
             (is (clojure.string/includes? (.title page) \"Example\"))))

         (step \"Verify heading\"
           (fn []
             (let [h1 (.locator page \"h1\")]
               (is (= \"Example Domain\" (.textContent h1))))))))
  "
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as t])
  (:import
   [com.microsoft.playwright
    APIRequest$NewContextOptions
    Browser$NewContextOptions
    BrowserType$LaunchOptions
    Page$ScreenshotOptions
     Playwright
     Playwright$CreateOptions
     Tracing$StartOptions
    Tracing$StopOptions]
   [com.microsoft.playwright.options ColorScheme]
   [java.nio.file Paths]))

;; ---------------------------------------------------------------------------
;; Internal helpers
;; ---------------------------------------------------------------------------

(defn- ensure-dir
  "Create directory (and parents) if it does not exist."
  [^String path]
  (let [f (io/file path)]
    (when-not (.exists f)
      (.mkdirs f))))

(defn- safe-name
  "Convert an arbitrary string into a filesystem-safe slug."
  [s]
  (-> s
      (str/replace #"[^a-zA-Z0-9_-]" "_")
      (str/replace #"_+" "_")))

(defn- trace-path
  "Return a java.nio.file.Path for the trace zip of the given test name."
  [test-name output-dir]
  (let [ts   (System/currentTimeMillis)
        file (str (safe-name test-name) "-" ts ".zip")]
    (Paths/get output-dir (into-array String [file]))))

(defn- rename-with-status
  "Rename <slug>-<ts>.zip to <slug>-PASS-<ts>.zip or <slug>-FAIL-<ts>.zip.
   Returns the new Path, or the original if renaming fails."
  [^java.nio.file.Path path status]
  (try
    (let [filename (.toString (.getFileName path))
          [_ slug ts] (re-matches #"^(.*)-(\d{13})\.zip$" filename)]
      (if (and slug ts)
        (let [new-name (str slug "-" (str/upper-case (name status)) "-" ts ".zip")
              new-path (.resolveSibling path new-name)]
          (java.nio.file.Files/move path new-path
            (into-array java.nio.file.CopyOption
                        [java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
          new-path)
        path))
    (catch Exception e
      (println "Warning: could not rename trace file:" (.getMessage e))
      path)))

;; ---------------------------------------------------------------------------
;; Public API — browser / tracing lifecycle
;; ---------------------------------------------------------------------------

(defn start-browser
  "Start a Playwright browser context with tracing enabled.

   Options:
     :headless    — boolean, default true.
                     Overridden by JVM property -Dplaywright.browser.headless=false
     :browser     — :chromium (default) | :webkit | :firefox.
                     Defaults from JVM property -Dplaywright.browser or env
                     PW_BROWSER, so a whole suite can be pointed at another
                     engine (e.g. WebKit ≈ Safari) without editing tests.
     :test-name   — string used to derive the trace filename.
     :output-dir  — directory for trace zips, default 'target/pw-traces'.
     :slow-mo     — milliseconds to slow down actions (useful headed), default 0.
     :executable-path — Chromium binary to drive instead of Playwright's own.
                     Defaults from -Dplaywright.browser.executable-path or env
                     PLAYWRIGHT_CHROME.
     :timeout-ms  — default timeout of every action. Defaults from
                     -Dplaywright.timeout.ms, else Playwright's 30s.
     :viewport    — [width height].
     :device-scale-factor — e.g. 2 for sharp screenshots.
     :color-scheme — :light | :dark.

   Returns a map with:
     :playwright  :browser  :context  :page  :trace-path"
  ([] (start-browser {}))
  ([opts]
   (let [headless?  (if (contains? opts :headless)
                      (:headless opts)
                       (not= "false" (System/getProperty "playwright.browser.headless" "true")))
         test-name  (or (:test-name opts) "test")
         output-dir (or (:output-dir opts)
                        (System/getProperty "pw.outputDirectory" "target/pw-traces"))
         slow-mo    (or (:slow-mo opts) (if headless? 0 80))
         _          (ensure-dir output-dir)
          tpath      (trace-path test-name output-dir)
          ;; Workaround for envs where Playwright tries to install browsers on
          ;; first use (e.g. unsupported OS). Use the already-installed browsers
          ;; under PLAYWRIGHT_BROWSERS_PATH and skip the bundled-browser install.
          pw-env     {"PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD" "1"
                      "PLAYWRIGHT_BROWSERS_PATH"
                      (or (System/getenv "PLAYWRIGHT_BROWSERS_PATH")
                          (str (System/getProperty "user.home")
                               "/.cache/ms-playwright"))}
          pw         (Playwright/create
                       (doto (Playwright$CreateOptions.) (.setEnv pw-env)))
         ;; Optional Chromium DNS override for networks whose resolver blocks
         ;; answers pointing at 127.0.0.1 (DNS-rebind protection on home
         ;; routers). Example:
         ;;   -Dplaywright.host-resolver-rules="MAP *.localdevhub.com 127.0.0.1"
         resolver-rules (or (:host-resolver-rules opts)
                            (System/getProperty "playwright.host-resolver-rules")
                            (System/getenv "PW_HOST_RESOLVER_RULES"))
         executable (or (:executable-path opts)
                        (System/getProperty "playwright.browser.executable-path")
                        (System/getenv "PLAYWRIGHT_CHROME"))
         lo         (cond-> (-> (BrowserType$LaunchOptions.)
                                (.setHeadless (boolean headless?))
                                (.setSlowMo (double slow-mo)))
                      resolver-rules
                      (.setArgs [(str "--host-resolver-rules=" resolver-rules)])

                      executable
                      (.setExecutablePath (Paths/get executable (into-array String []))))
         browser-kw (or (:browser opts)
                        (keyword (System/getProperty
                                  "playwright.browser"
                                  (or (System/getenv "PW_BROWSER") "chromium"))))
         browser    (.launch (case browser-kw
                               :webkit  (.webkit pw)
                               :firefox (.firefox pw)
                               (.chromium pw))
                             lo)
         ignore-https? (if (contains? opts :ignore-https-errors)
                         (:ignore-https-errors opts)
                         (= "true" (System/getProperty
                                    "playwright.ignore-https-errors" "false")))
         {:keys [viewport device-scale-factor color-scheme]} opts
         context    (.newContext browser
                                 (cond-> (Browser$NewContextOptions.)
                                   ignore-https? (.setIgnoreHTTPSErrors true)

                                   viewport
                                   (.setViewportSize (int (first viewport)) (int (second viewport)))

                                   device-scale-factor
                                   (.setDeviceScaleFactor (double device-scale-factor))

                                   color-scheme
                                   (.setColorScheme (if (= :dark color-scheme)
                                                      ColorScheme/DARK
                                                      ColorScheme/LIGHT))))
         timeout-ms (or (:timeout-ms opts)
                        (some-> (System/getProperty "playwright.timeout.ms") Long/parseLong))
         _          (when timeout-ms
                      (.setDefaultTimeout context (double timeout-ms)))
         ;; Start tracing: screenshots + DOM snapshots + source locations
         _          (-> (.tracing context)
                        (.start (-> (Tracing$StartOptions.)
                                    (.setScreenshots true)
                                    (.setSnapshots true)
                                    (.setSources true))))
         page       (.newPage context)
         ;; Surface failed non-GET requests (commands, queries, uploads) in the
         ;; test output with the browser's failure reason. Tracing records dead
         ;; requests without their error text (no _failureText in trace.network),
         ;; so things like net::ERR_NETWORK_CHANGED or net::ERR_ABORTED (request
         ;; killed by navigation) are otherwise only visible via console-event
         ;; archaeology in the trace viewer. GETs are skipped — failing image /
         ;; presigned-URL fetches are routine noise.
         _          (.onRequestFailed page
                      (reify java.util.function.Consumer
                        (accept [_ request]
                          (let [method (.method ^com.microsoft.playwright.Request request)]
                            (when-not (= "GET" method)
                              (println (format "PW request FAILED: %s %s -> %s"
                                               method
                                               (.url ^com.microsoft.playwright.Request request)
                                               (.failure ^com.microsoft.playwright.Request request))))))))]
     (when-not headless?
       (println "Browser launched in HEADED mode"))
     (println (str "Trace recording started -> " (.toString tpath)))
     {:playwright pw
       :browser    browser
       :context    context
       :page       page
       :trace-path tpath
       :headed?    (not headless?)})))

(defn stop-browser
  "Stop tracing, save trace.zip, rename it with status suffix, then close everything.
   `status` is :pass or :fail (default :pass).
   Returns the java.nio.file.Path of the saved (and renamed) trace, or nil on error."
  ([bm] (stop-browser bm :pass))
  ([{:keys [playwright browser context page trace-path] :as _bm} status]
   (try
     (when page    (.close page))
     (when context
       (-> (.tracing context)
           (.stop (-> (Tracing$StopOptions.)
                      (.setPath trace-path))))
       (.close context))
     (when browser (.close browser))
     (when playwright (.close playwright))
     (when (and trace-path (.exists (.toFile trace-path)))
       (let [final-path (rename-with-status trace-path status)]
         (println (str "Trace saved: " (.toString final-path)))
         final-path))
     (catch Exception e
       (println "Warning: error closing browser:" (.getMessage e))
       nil))))

;; ---------------------------------------------------------------------------
;; Dynamic vars
;; ---------------------------------------------------------------------------

(def ^:dynamic *current-page*
  "Dynamically bound to the Playwright Page inside `with-pw`.
   `step` and `pw-screenshot` read this automatically so callers never need to
   pass page/context explicitly."
  nil)

(def ^:dynamic *current-api*
  "Dynamically bound to the Playwright APIRequestContext inside `with-api`.
   Allows nested helpers to access the REST client without explicit threading."
  nil)

(def ^:dynamic *verbose-steps*
  "When true, `step` prints each step name, timing, and pass/fail to stdout.
   Automatically set to true when running headed (`:headless false` or
   `-Dplaywright.browser.headless=false`).  Can also be bound manually."
  false)

;; ---------------------------------------------------------------------------
;; Public API — step annotations
;; ---------------------------------------------------------------------------

(defn begin-step
  "Open a named group in the Playwright trace.
   Internal — use the `step` macro instead."
  [context description]
  (try
    (.group (.tracing context) description)
    (catch Exception e
      (println "Warning: could not open trace group:" (.getMessage e)))))

(defn end-step
  "Close the most recently opened trace group.
   Internal — use the `step` macro instead."
  [context]
  (try
    (.groupEnd (.tracing context))
    (catch Exception e
      (println "Warning: could not close trace group:" (.getMessage e)))))

(defmacro step
  "Execute f inside a named Playwright trace group.

     (step \"Log in\"
       (fn []
         (.fill page \"#user\" \"admin\")
         (.click page \"#submit\")))

   Reads the current page from `*current-page*` (bound by `with-pw`).
   The step name appears as a collapsible group in the Trace Viewer action list.

   When `*verbose-steps*` is true (automatically set in headed mode), step
   names, timings and pass/fail status are printed to stdout."
  [description f]
  `(let [ctx#     (.context *current-page*)
         verbose# *verbose-steps*
         start#   (System/currentTimeMillis)]
     (when verbose# (println (str "  => " ~description)))
     (testing.pw/begin-step ctx# ~description)
     (try
       (let [result# (~f)]
         (testing.pw/end-step ctx#)
         (when verbose#
           (println (str "     ok (" (- (System/currentTimeMillis) start#) "ms)")))
         result#)
       (catch Exception e#
         (testing.pw/end-step ctx#)
         (when verbose#
           (println (str "     FAILED: " (.getMessage e#))))
         (throw e#)))))

;; ---------------------------------------------------------------------------
;; Screenshot attachment
;; ---------------------------------------------------------------------------

(defn pw-screenshot
  "Take a screenshot and add it as an attachment annotation inside the
   current trace context.  Returns the screenshot bytes.

   Reads the current page from `*current-page*` (bound by `with-pw`).

   Because Playwright's tracing automatically captures screenshots on every
   action, this is only needed for *explicit* labelled snapshots outside of
   normal user interactions."
  [^String label]
  (try
    (let [bytes (.screenshot *current-page*
                              (-> (Page$ScreenshotOptions.)
                                  (.setFullPage false)))]
      (println (str "  [screenshot] " label " (" (alength bytes) " bytes)"))
      bytes)
    (catch Exception e
      (println (str "Warning: screenshot failed: " (.getMessage e)))
      nil)))

;; ---------------------------------------------------------------------------
;; Main test macro
;; ---------------------------------------------------------------------------

(defmacro with-ui
  "Main test wrapper that provides a Playwright Page bound to `page-binding`
   and full tracing lifecycle management.  Also binds `*current-page*` so
   `step` and `pw-screenshot` can be used without passing page explicitly.

   Forms:

     (with-ui [page]
       (step \"Do something\" #(.navigate page \"https://...\"))
       (step \"Verify\" #(is (= \"Title\" (.title page)))))

     ;; with explicit options:
     (with-ui [page {:headless false :output-dir \"target/my-traces\"}]
       ...)

   Options (second element of binding vector, optional map):
     :headless    — false to show a browser window (default true)
     :output-dir  — directory for trace zips (default 'target/pw-traces')
     :slow-mo     — ms delay between actions when headed (default 80)

   What happens:
     1. Chromium is launched, tracing started (screenshots + snapshots + sources).
     2. Body forms execute with `page` bound to the Playwright Page and
        `*current-page*` dynamically bound to the same Page.
     3. Tracing stops and trace.zip is written to output-dir.
     4. Browser is closed regardless of test outcome.
     5. On success/failure the trace path is printed for quick opening.

   Viewing a trace:
     npx playwright show-trace target/pw-traces/<name>.zip
     # or drag-drop the zip to https://trace.playwright.dev
  "
  [[page-binding & [opts]] & body]
  `(let [test-name#  (or (some-> t/*testing-vars* first meta :name str)
                         "unknown-test")
         bm#         (start-browser (merge {:test-name test-name#}
                                            ~(or opts {})))
         ~page-binding (:page bm#)]
     (println (str "\n" (apply str (repeat 60 "=")) "\n"
                   "PW Test: " test-name# "\n"
                   (apply str (repeat 60 "="))))
     ;; Intercept clojure.test reports so we can reflect failures in output.
     (let [failures# (atom [])
           orig-report# t/report]
        (try
          (binding [*current-page*  ~page-binding
                    *verbose-steps* (:headed? bm#)]
            (with-redefs [t/report (fn [m#]
                                     (when (#{:fail :error} (:type m#))
                                       (swap! failures# conj m#))
                                     (orig-report# m#))]
              (do ~@body)))
         (let [failed?# (seq @failures#)
               tp#      (stop-browser bm# (if failed?# :fail :pass))]
           (if failed?#
             (do
               (println (str "\nFAILED: " test-name#))
               (println (str "Trace -> " (when tp# (.toString tp#)))))
             (do
               (println (str "\nPASSED: " test-name#))
               (println (str "Trace -> " (when tp# (.toString tp#))))))
           (println (str "View  -> npx playwright show-trace "
                         (when tp# (.toString tp#)))))
         (catch Exception e#
           (let [tp# (stop-browser bm# :fail)]
             (println (str "\nERROR: " test-name# " — " (.getMessage e#)))
             (println (str "Trace -> " (when tp# (.toString tp#)))))
           (throw e#))
         (finally
           ;; idempotent close guard
           (try (stop-browser bm#) (catch Exception ~'_)))))))

(defmacro with-pw
  "Deprecated alias for `with-ui`. Use `with-ui` instead."
  [binding & body]
  `(with-ui ~binding ~@body))

;; ---------------------------------------------------------------------------
;; REST client macro
;; ---------------------------------------------------------------------------

(defmacro with-api
  "Provide a Playwright APIRequestContext bound to `client-binding`.

   Behaviour depends on whether a browser session is already active:

   * Inside `with-pw` (`*current-page*` is bound):
       BrowserContext.request() returns the context's own APIRequestContext
       directly — no factory step needed.  HTTP calls appear in the trace.
       The browser lifecycle is untouched.

   * Standalone (no `*current-page*`):
       Starts a fresh headless Chromium + BrowserContext with tracing enabled,
       produces a trace.zip just like `with-pw`, then closes everything.
       Uses Playwright.request().newContext(opts) to create the REST client.

   Options map (optional second element of binding vector):
     :ignore-https-errors — boolean, default false (standalone mode only).
     All other options accepted by `start-browser` (:headless, :output-dir,
     :slow-mo) are forwarded when starting a standalone context.

   Examples:

     ;; Standalone REST-only test — full trace produced
     (deftest config-fetch-test
       (with-api [client]
         (let [resp (.get client \"https://jsonplaceholder.typicode.com/posts/1\" nil)]
           (is (= 200 (.status resp))))))

     ;; Inside with-pw — HTTP recorded in the same browser trace
     (deftest mixed-test
       (with-pw [page]
         (step \"Load UI\" #(.navigate page \"https://example.com\"))
         (with-api [client]
           (let [resp (.get client \"https://jsonplaceholder.typicode.com/posts/1\" nil)]
             (is (= 200 (.status resp)))))))
  "
  [[client-binding & [opts]] & body]
  `(let [opts# ~(or opts {})]
     (if *current-page*
       ;; ---- Attached mode -------------------------------------------------------
       ;; BrowserContext.request() returns an APIRequestContext directly.
       ;; No disposal needed — it is owned by the BrowserContext lifecycle.
       (let [~client-binding (.request (.context *current-page*))]
         (binding [*current-api* ~client-binding]
           (do ~@body)))
       ;; ---- Standalone mode -----------------------------------------------------
       ;; Playwright.request() -> APIRequest factory -> .newContext(opts) -> client.
       (let [test-name# (or (some-> t/*testing-vars* first meta :name str)
                            "unknown-test")
             bm#        (start-browser (merge {:test-name test-name#} opts#))
             ~client-binding
             (-> (:playwright bm#)
                 (.request)
                 (.newContext (cond-> (APIRequest$NewContextOptions.)
                                (:ignore-https-errors opts#)
                                (.setIgnoreHTTPSErrors true))))]
         (let [failures# (atom [])
               orig-report# t/report]
           (try
              (binding [*current-page*  (:page bm#)
                        *current-api*   ~client-binding
                        *verbose-steps* (:headed? bm#)]
               (with-redefs [t/report (fn [m#]
                                        (when (#{:fail :error} (:type m#))
                                          (swap! failures# conj m#))
                                        (orig-report# m#))]
                 (do ~@body)))
             (finally
               (try (.dispose ~client-binding) (catch Exception ~'_))
               (let [failed?# (seq @failures#)
                     tp#      (stop-browser bm# (if failed?# :fail :pass))]
                 (if failed?#
                   (println (str "\nFAILED: " test-name# "\nTrace -> " (when tp# (.toString tp#))))
                   (println (str "\nPASSED: " test-name# "\nTrace -> " (when tp# (.toString tp#)))))
                 (println (str "View  -> npx playwright show-trace "
                               (when tp# (.toString tp#))))))))))))
