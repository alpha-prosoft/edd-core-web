(ns testing.pw-report
  "Playwright trace aggregation and HTML dashboard generator.

   Scans a directory tree for *.zip trace files produced by `testing.pw/with-pw`
   and emits a self-contained `index.html` dashboard that:

     - Lists every trace with its test name, timestamp, and file size.
     - Provides direct links to open each trace in the official online viewer
       (https://trace.playwright.dev) — works when you serve the traces over HTTP.
     - Shows a local CLI command to open each trace with the Playwright CLI.
     - Includes an aggregated status summary (total / by date).
     - Copies the Playwright Trace Viewer static assets into <output-dir>/trace/
       so that the embedded offline viewer links work immediately.

   Usage from the REPL or as a -main entry point:

     (require '[testing.pw-report :as rep])
     (rep/generate-report)                       ; default: scans target/pw-traces
     (rep/generate-report {:traces-dir \"target/pw-traces\"
                            :output-dir \"target/pw-traces\"
                            :project    \"My Project\"})

   CLI alias (see deps.edn :pw-report):
     clojure -M:pw-report
  "
  (:require
   [clojure.java.io   :as io]
   [clojure.string    :as str]
   [testing.pw-assets :as assets])
  (:import
   [java.io   File]
   [java.text SimpleDateFormat]
   [java.util Date]))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(defn- ensure-dir
  "Create directory (and parents) if it does not exist."
  [^String path]
  (let [f (io/file path)]
    (when-not (.exists f)
      (.mkdirs f))))

(defn- human-bytes [n]
  (cond
    (< n 1024)       (str n " B")
    (< n 1048576)    (format "%.1f KB" (/ n 1024.0))
    :else            (format "%.2f MB" (/ n 1048576.0))))

(defn- parse-trace-meta
  "Extract test name, status and timestamp from a trace zip filename.
   Expected format: <safe-test-name>-PASS-<epoch-ms>.zip
                 or <safe-test-name>-FAIL-<epoch-ms>.zip
                 or <safe-test-name>-<epoch-ms>.zip  (legacy, no status)"
  [^File f]
  (let [base (-> (.getName f)
                 (str/replace #"\.zip$" ""))
        ;; Try new format with status first
        with-status  (re-matches #"^(.*?)-(PASS|FAIL)-(\d{13})$" base)
        ;; Fall back to legacy format without status
        without-status (when-not with-status
                         (re-matches #"^(.*)-(\d{13})$" base))
        [slug status ts] (cond
                           with-status    [(nth with-status 1)
                                           (str/lower-case (nth with-status 2))
                                           (nth with-status 3)]
                           without-status [(nth without-status 1) "unknown"
                                           (nth without-status 2)]
                           :else          [nil "unknown" nil])
        test-name (if slug (str/replace slug #"_" " ") base)
        timestamp (when ts (Long/parseLong ts))]
    {:file      f
     :name      test-name
     :status    status
     :timestamp timestamp
     :size      (.length f)
     :filename  (.getName f)}))

(defn- format-ts [^Long ts]
  (when ts
    (.format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (Date. ts))))

(defn- collect-traces
  "Return a seq of trace metadata maps from all *.zip files found under dir."
  [^String dir-path]
  (->> (file-seq (io/file dir-path))
       (filter #(and (.isFile ^File %)
                     (str/ends-with? (.getName ^File %) ".zip")))
       (map parse-trace-meta)
       (sort-by (fn [m] (or (:timestamp m) 0)) #(compare %2 %1)))) ; newest first

;; ---------------------------------------------------------------------------
;; HTML generation
;; ---------------------------------------------------------------------------

(defn- badge [text color]
  (str "<span style=\"display:inline-block;padding:2px 8px;border-radius:4px;"
       "font-size:11px;font-weight:600;background:" color ";color:#fff;\">"
       text "</span>"))

(defn- status-badge [status]
  (case status
    "pass"    (badge "PASS" "#2da44e")
    "fail"    (badge "FAIL" "#cf222e")
    (badge "?" "#888")))

(defn- trace-row
  "Render a single <tr> for one trace entry."
  [{:keys [name timestamp size filename status]} traces-dir]
  (let [ts-str   (or (format-ts timestamp) "—")
        view-cmd (str "npx playwright show-trace " traces-dir "/" filename)
        offline-url (str "trace/index.html?trace=../" filename)]
    (str "<tr>"
         "<td style=\"padding:8px 12px;\">" (status-badge status) "</td>"
         "<td style=\"padding:8px 12px;font-weight:500;\">" (or name "Unknown") "</td>"
         "<td style=\"padding:8px 12px;color:#666;font-size:13px;\">" ts-str "</td>"
         "<td style=\"padding:8px 12px;color:#888;font-size:12px;\">" (human-bytes size) "</td>"
         "<td style=\"padding:8px 12px;\">"
         "  <code style=\"font-size:11px;background:#f4f4f4;padding:2px 6px;border-radius:3px;display:block;white-space:nowrap;overflow:auto;\">"
         view-cmd
         "  </code>"
         "</td>"
         "<td style=\"padding:8px 12px;\">"
         "  <a href=\"" offline-url "\" "
         "     style=\"font-size:12px;color:#0078d4;text-decoration:none;font-weight:500;\">"
         "    Open Trace Viewer &#8599;"
         "  </a>"
         "</td>"
         "</tr>")))

(defn- render-html
  "Build the full HTML string for the aggregate report."
  [traces project traces-dir]
  (let [total    (count traces)
        passed   (count (filter #(= "pass" (:status %)) traces))
        failed   (count (filter #(= "fail" (:status %)) traces))
        now-str  (.format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (Date.))
        rows     (str/join "\n" (map #(trace-row % traces-dir) traces))]
    (str
     "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n"
     "<meta charset=\"UTF-8\">\n"
     "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n"
     "<title>" project " — Playwright Trace Report</title>\n"
     "<style>\n"
     "  body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;"
     "       margin:0;background:#fafafa;color:#1a1a1a;}\n"
     "  header{background:#0d1117;color:#fff;padding:20px 32px;}\n"
     "  header h1{margin:0;font-size:22px;font-weight:600;}\n"
     "  header p{margin:4px 0 0;font-size:13px;color:#8b949e;}\n"
     "  .container{max-width:1200px;margin:24px auto;padding:0 24px;}\n"
     "  .card{background:#fff;border:1px solid #e0e0e0;border-radius:8px;"
     "        box-shadow:0 1px 3px rgba(0,0,0,.06);overflow:hidden;margin-bottom:24px;}\n"
     "  .card-header{padding:14px 20px;border-bottom:1px solid #e0e0e0;"
     "               font-weight:600;font-size:14px;background:#f8f8f8;}\n"
     "  table{width:100%;border-collapse:collapse;font-size:13px;}\n"
     "  th{padding:10px 12px;text-align:left;font-size:12px;font-weight:600;"
     "     color:#555;border-bottom:2px solid #e0e0e0;background:#f8f8f8;}\n"
     "  tr:hover td{background:#f5f8ff;}\n"
     "  .stat{display:inline-block;margin:0 12px;text-align:center;}\n"
     "  .stat .n{font-size:32px;font-weight:700;color:#0078d4;}\n"
     "  .stat .l{font-size:12px;color:#888;margin-top:4px;}\n"
     "  .instructions{background:#0d1117;color:#e6edf3;padding:16px 20px;"
     "                border-radius:6px;font-size:13px;line-height:1.6;}\n"
     "  .instructions code{color:#79c0ff;}\n"
     "  footer{text-align:center;padding:20px;font-size:12px;color:#999;}\n"
     "</style>\n</head>\n<body>\n"
     "<header>\n"
     "  <h1>" project "</h1>\n"
     "  <p>Playwright Trace Report &mdash; generated " now-str "</p>\n"
     "</header>\n"
     "<div class=\"container\">\n"

     ;; Summary stats
     "<div class=\"card\">\n"
     "  <div class=\"card-header\">Summary</div>\n"
     "  <div style=\"padding:20px;\">\n"
     "    <div class=\"stat\"><div class=\"n\">" total "</div><div class=\"l\">Total</div></div>\n"
     "    <div class=\"stat\" style=\"color:#2da44e;\"><div class=\"n\" style=\"color:#2da44e;\">" passed "</div><div class=\"l\">Passed</div></div>\n"
     "    <div class=\"stat\" style=\"color:#cf222e;\"><div class=\"n\" style=\"color:#cf222e;\">" failed "</div><div class=\"l\">Failed</div></div>\n"
     "  </div>\n"
     "</div>\n"

     ;; Trace table
     "<div class=\"card\">\n"
     "  <div class=\"card-header\">Traces (" total ")</div>\n"
     "  <table>\n"
     "    <thead><tr>\n"
     "      <th>Status</th><th>Test Name</th><th>Recorded At</th><th>Size</th>"
     "      <th>CLI Command</th><th>Offline Viewer</th>\n"
     "    </tr></thead>\n"
     "    <tbody>\n"
     rows "\n"
     "    </tbody>\n"
     "  </table>\n"
     "</div>\n"

     "</div>\n"
     "<footer>Generated by <strong>testing.pw-report</strong> &mdash; "
     "<a href=\"https://playwright.dev/docs/trace-viewer\" target=\"_blank\">Playwright Trace Viewer docs</a>"
     "</footer>\n"
     "</body>\n</html>\n")))

;; ---------------------------------------------------------------------------
;; Public entry point
;; ---------------------------------------------------------------------------

(defn generate-report
  "Scan `traces-dir` for *.zip Playwright trace files and write an HTML
   aggregate dashboard to `output-dir`/index.html.

   Options (all optional):
     :traces-dir  — directory to scan for .zip files
                    (default: system property pw.outputDirectory or 'target/pw-traces')
     :output-dir  — where to write index.html
                    (default: same as traces-dir)
     :project     — project name shown in the report header
                    (default: system property pw.project.name or 'Playwright Tests')

   Returns the java.io.File pointing to the generated index.html."
  ([] (generate-report {}))
  ([{:keys [traces-dir output-dir project]}]
   (let [td   (or traces-dir
                  (System/getProperty "pw.outputDirectory" "target/pw-traces"))
         od   (or output-dir td)
         proj (or project
                  (System/getProperty "pw.project.name" "Playwright Tests"))
         _    (ensure-dir od)
         _    (assets/copy-trace-viewer od)
         traces (collect-traces td)
         html   (render-html traces proj td)
          out-f  (io/file od "index.html")]
     (spit out-f html)
     out-f)))

(defn -main
  "CLI entry point.  Reads options from system properties:
     -Dpw.outputDirectory=target/pw-traces
     -Dpw.project.name=My Project"
  [& _args]
  (generate-report)
  (System/exit 0))
