(ns testing.pw-http-server
  "Pure-Java HTTP server that serves the pw-report output directory
   (target/pw-traces by default) so the embedded offline Trace Viewer
   works correctly in a browser.

   The server is built entirely on `com.sun.net.httpserver.HttpServer`,
   which ships with every JDK — no extra dependencies required.

   Usage:
     clojure -M:traces

   JVM properties (all optional):
     -Dpw.server.port=<n>          HTTP port (default: 8080)
     -Dpw.server.dir=<path>        Directory to serve (default: target/pw-traces)

   Once started, open http://localhost:<port> in your browser.
   Press Ctrl-C to stop."
  (:require
   [clojure.java.io :as io]
   [clojure.string  :as str])
  (:import
   [com.sun.net.httpserver HttpExchange HttpHandler HttpServer]
   [java.io File]
   [java.net InetSocketAddress]
   [java.nio.file Files]))

;; ---------------------------------------------------------------------------
;; MIME type table
;; ---------------------------------------------------------------------------

(def ^:private mime-types
  {"html" "text/html; charset=utf-8"
   "htm"  "text/html; charset=utf-8"
   "css"  "text/css; charset=utf-8"
   "js"   "application/javascript; charset=utf-8"
   "mjs"  "application/javascript; charset=utf-8"
   "json" "application/json; charset=utf-8"
   "png"  "image/png"
   "jpg"  "image/jpeg"
   "jpeg" "image/jpeg"
   "svg"  "image/svg+xml"
   "ico"  "image/x-icon"
   "wasm" "application/wasm"
   "zip"  "application/zip"
   "txt"  "text/plain; charset=utf-8"
   "map"  "application/json"})

(defn- content-type [^File f]
  (let [name (.getName f)
        ext  (last (str/split name #"\."))]
    (get mime-types (str/lower-case ext) "application/octet-stream")))

;; ---------------------------------------------------------------------------
;; Request handler
;; ---------------------------------------------------------------------------

(defn- make-handler
  "Returns an HttpHandler that serves files rooted at `root-dir`."
  [^File root-dir]
   (reify HttpHandler
     (^void handle [_ ^HttpExchange exchange]
       (let [method   (.getRequestMethod exchange)
             raw-path (-> exchange .getRequestURI .getPath)
             rel-path (cond
                        (or (= raw-path "/") (= raw-path "")) "index.html"
                        :else (str/replace-first raw-path #"^/" ""))
             target   (io/file root-dir rel-path)]
         (cond
           ;; Only GET/HEAD
           (not (#{"GET" "HEAD"} method))
           (do (.sendResponseHeaders exchange 405 -1)
               (.close (.getResponseBody exchange)))

          ;; File exists and is readable
          (and (.exists target) (.isFile target) (.canRead target))
          (let [ct      (content-type target)
                content (Files/readAllBytes (.toPath target))
                len     (alength content)]
            (doto (.getResponseHeaders exchange)
              (.set "Content-Type" ct)
              (.set "Cache-Control" "no-cache"))
            (.sendResponseHeaders exchange 200 len)
            (when (= method "GET")
              (with-open [out (.getResponseBody exchange)]
                (.write out content))))

          ;; Directory — try index.html inside
          (and (.exists target) (.isDirectory target))
          (let [idx (io/file target "index.html")]
            (if (and (.exists idx) (.isFile idx))
              (let [ct      (content-type idx)
                    content (Files/readAllBytes (.toPath idx))
                    len     (alength content)]
                (doto (.getResponseHeaders exchange)
                  (.set "Content-Type" ct)
                  (.set "Cache-Control" "no-cache"))
                (.sendResponseHeaders exchange 200 len)
                (when (= method "GET")
                  (with-open [out (.getResponseBody exchange)]
                    (.write out content))))
              (do (.sendResponseHeaders exchange 404 -1)
                  (.close (.getResponseBody exchange)))))

          ;; Not found
          :else
          (let [msg (.getBytes (str "404 Not Found: " rel-path) "UTF-8")]
            (doto (.getResponseHeaders exchange)
              (.set "Content-Type" "text/plain; charset=utf-8"))
            (.sendResponseHeaders exchange 404 (alength msg))
            (when (= method "GET")
              (with-open [out (.getResponseBody exchange)]
                (.write out msg)))))
        nil))))

;; ---------------------------------------------------------------------------
;; Server lifecycle
;; ---------------------------------------------------------------------------

(defn start-server
  "Start the HTTP server.

   Options (all optional):
     :port  — TCP port to listen on (default: 8080,
               or JVM property pw.server.port)
     :dir   — directory to serve  (default: \"target/pw-traces\",
               or JVM property pw.server.dir)

   Returns the running HttpServer instance."
  ([] (start-server {}))
  ([{:keys [port dir]}]
   (let [port (or port
                  (some-> (System/getProperty "pw.server.port") Integer/parseInt)
                  8080)
         dir  (or dir
                  (System/getProperty "pw.server.dir" "target/pw-traces"))
         root (io/file dir)]
     (when-not (.exists root)
       (println (str "WARNING: serve directory does not exist: " (.getAbsolutePath root)))
       (println "         Run `clojure -M:aggregate` first to generate the report."))
     (let [server (HttpServer/create (InetSocketAddress. port) 0)]
       (.createContext server "/" (make-handler root))
       (.setExecutor server nil)
       (.start server)
       (println (str "\nTrace Viewer HTTP server started."))
       (println (str "  Serving : " (.getAbsolutePath root)))
        (println (str "  URL     : http://localhost:" port))
       (println "  Press Ctrl-C to stop.\n")
       server))))

(defn stop-server
  "Stop a running HttpServer (waits 0 seconds for in-flight requests)."
  [^HttpServer server]
  (.stop server 0))

;; ---------------------------------------------------------------------------
;; Entry point
;; ---------------------------------------------------------------------------

(defn -main [& _args]
  (start-server)
  ;; Park the main thread — the HTTP server runs on a daemon executor, so we
  ;; must prevent the JVM from exiting.  Ctrl-C (SIGINT) will terminate it.
  @(promise))
