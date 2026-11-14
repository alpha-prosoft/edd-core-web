(ns testing.pw-assets
  "Locates and copies Playwright Trace Viewer static assets.

   Shared between testing.pw-report and testing.pw-aggregate so that both
   can embed the offline viewer without a circular dependency."
  (:require
   [clojure.java.io :as io]
   [clojure.string  :as str])
  (:import
   [java.io File]))

(defn find-trace-viewer-dir
  "Locate the playwright-core traceViewer static assets directory.

   Strategy 1: ask Node.js to resolve playwright-core (works when
               playwright-core is available as a local or global npm package).
   Strategy 2: walk well-known npx cache locations (~/.npm/_npx)."
  []
  (or
   ;; Strategy 1 — Node.js require resolution
   (try
     (let [script (str "try{"
                       "const p=require.resolve('playwright-core/package.json');"
                       "const path=require('path');"
                       "console.log(path.join(path.dirname(p),'lib','vite','traceViewer'));"
                       "}catch(e){process.exit(1)}")
           proc   (-> (ProcessBuilder. ^java.util.List ["node" "-e" script])
                      (.start))
           out    (-> proc .getInputStream io/reader slurp str/trim)
           _      (.waitFor proc)
           f      (io/file out)]
       (when (and (pos? (count out)) (.isDirectory f) (.exists (io/file f "index.html")))
         f))
     (catch Exception _ nil))

   ;; Strategy 2 — npx cache fallback (~/.npm/_npx/**)
   (let [npx-dir (io/file (System/getProperty "user.home") ".npm" "_npx")]
     (when (.exists npx-dir)
       (->> (file-seq npx-dir)
            (filter #(and (.isDirectory ^File %)
                          (= (.getName ^File %) "traceViewer")))
            (filter #(.exists (io/file % "index.html")))
            first)))))

(defn copy-trace-viewer
  "Copy Playwright Trace Viewer static assets into <traces-dir>/trace/.
   Returns the destination directory File, or nil if the source could not be found."
  [^String traces-dir]
  (let [src (find-trace-viewer-dir)]
    (if (nil? src)
      (do (println "  WARNING: Could not locate playwright-core traceViewer assets.")
          (println "           Offline viewer will not be embedded.")
          nil)
      (let [dest (io/file traces-dir "trace")]
        (.mkdirs dest)
        (doseq [^File f (file-seq src)
                :when (.isFile f)]
          (let [rel  (str/replace (.getAbsolutePath f)
                                  (str (.getAbsolutePath src) File/separator)
                                  "")
                out  (io/file dest rel)]
            (.mkdirs (.getParentFile out))
            (io/copy f out)))
        (println (str "  Trace viewer assets copied → " (.getAbsolutePath dest)))
        dest))))
