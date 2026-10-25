(ns web.gallery.it.browser
  "Optionally points clojure-playwright at an already-installed Chromium
   instead of Playwright's bundled browser.

   Playwright can drive any compatible Chromium via
   BrowserType.LaunchOptions.setExecutablePath, so we replace
   testing.pw/start-browser with a variant that sets that path when configured.

   Configure the path with either:
     -Dplaywright.browser.executable-path=/opt/chrome-linux64/chrome   (JVM prop)
     PLAYWRIGHT_CHROME=/opt/chrome-linux64/chrome                      (env var)

   When neither is set, start-browser is left untouched and Playwright uses its
   own bundled browser (the normal local-dev path)."
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [testing.pw :as pw])
  (:import
   [com.microsoft.playwright
    Browser$NewContextOptions
    BrowserType$LaunchOptions
    Playwright
    Tracing$StartOptions]
   [java.nio.file Paths]))

(defn configured-executable-path []
  (or (System/getProperty "playwright.browser.executable-path")
      (System/getenv "PLAYWRIGHT_CHROME")))

(defn- ensure-dir [^String path]
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (.mkdirs dir))))

(defn- safe-name [s]
  (-> s
      (str/replace #"[^a-zA-Z0-9_-]" "_")
      (str/replace #"_+" "_")))

(defn- trace-path [test-name output-dir]
  (let [ts   (System/currentTimeMillis)
        file (str (safe-name test-name) "-" ts ".zip")]
    (Paths/get output-dir (into-array String [file]))))

(defn- headless? [opts]
  (if (contains? opts :headless)
    (:headless opts)
    (not= "false" (System/getProperty "playwright.browser.headless" "true"))))

(defn- launch-options [opts ^String executable]
  (let [headless
        (headless? opts)

        slow-mo
        (or (:slow-mo opts) (if headless 0 80))]
    (-> (BrowserType$LaunchOptions.)
        (.setHeadless (boolean headless))
        (.setSlowMo (double slow-mo))
        (.setExecutablePath (Paths/get executable (into-array String []))))))

(defn start-browser-with-executable
  "Drop-in replacement for testing.pw/start-browser that launches the Chromium
   binary at `executable` rather than Playwright's bundled browser."
  [^String executable opts]
  (let [test-name
        (or (:test-name opts) "test")

        output-dir
        (or (:output-dir opts)
            (System/getProperty "pw.outputDirectory" "target/pw-traces"))

        _
        (ensure-dir output-dir)

        tpath
        (trace-path test-name output-dir)

        pw
        (Playwright/create)

        browser
        (.launch (.chromium pw) (launch-options opts executable))

        context
        (.newContext browser (Browser$NewContextOptions.))

        ;; Bulk tests visit every gallery page in one session; the 30s default
        ;; is tight under load, so allow an override via
        ;; -Dplaywright.timeout.ms (default 60s).
        _
        (.setDefaultTimeout
         context
         (double (Long/parseLong (System/getProperty "playwright.timeout.ms" "60000"))))

        _
        (-> (.tracing context)
            (.start (-> (Tracing$StartOptions.)
                        (.setScreenshots true)
                        (.setSnapshots true)
                        (.setSources true))))

        page
        (.newPage context)]
    (println (str "Trace recording started -> " (.toString tpath)))
    {:playwright pw
     :browser    browser
     :context    context
     :page       page
     :trace-path tpath
     :headed?    (not (headless? opts))}))

(defn install!
  "Patch testing.pw/start-browser to launch the configured Chromium. A no-op
   when no executable path is configured."
  []
  (when-let [executable (configured-executable-path)]
    (alter-var-root #'pw/start-browser
                    (constantly
                     (fn
                       ([] (start-browser-with-executable executable {}))
                       ([opts] (start-browser-with-executable executable opts)))))
    (println "Using Chromium executable:" executable)))
