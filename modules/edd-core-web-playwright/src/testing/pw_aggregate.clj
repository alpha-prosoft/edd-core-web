(ns testing.pw-aggregate
  "Aggregate report generator: copies Playwright Trace Viewer assets and
   builds the HTML report from existing trace zips.

   Usage:
     clojure -M:aggregate

   Run tests first with:
     clojure -M:test
  "
  (:require
   [clojure.java.io   :as io]
   [clojure.string    :as str]
   [testing.pw-assets :as assets]
   [testing.pw-report :as rep])
  (:import
   [java.io File]))

(defn copy-trace-viewer
  "Copy Playwright Trace Viewer static assets into <traces-dir>/trace/."
  [^String traces-dir]
  (assets/copy-trace-viewer traces-dir))

;; ---------------------------------------------------------------------------
;; Main entry point
;; ---------------------------------------------------------------------------

(defn run
  "Aggregate pipeline:
     1. Copy Playwright Trace Viewer static assets into <traces-dir>/trace/.
     2. Generate HTML report (index.html) from existing trace zips.

   Options map (all optional):
     :traces-dir — where trace zips live (default: target/pw-traces)"
  ([] (run {}))
  ([{:keys [traces-dir]
     :or   {traces-dir "target/pw-traces"}}]
   (copy-trace-viewer traces-dir)
   (let [report-file (rep/generate-report {:traces-dir traces-dir})]
     (println (str "Report: " (.getAbsolutePath report-file)))
     (println "View  : clojure -M:traces"))))

(defn -main [& _args]
  (run)
  (System/exit 0))
