(ns projectname.explore
  "Drives the nREPL browser (clojure -M:explore) with the same SDK and el-ids
   as the tests. Every helper binds the SDK to the current page first, so it
   keeps working after testing.repl/restart."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [projectname.helpers :as h]
            [testing.repl :as repl]
            [web.primitives.sdk.core :as sdk]
            [web.primitives.sdk.values :as values])
  (:import [com.microsoft.playwright Page$ScreenshotOptions]))

(defn bind!
  []
  (alter-var-root #'sdk/*page* (constantly @repl/page))
  @repl/page)

(defn open!
  [path]
  (bind!)
  (sdk/goto! (str h/base-url path))
  (.url (sdk/page)))

(defn- app-el-id?
  [id]
  (string/includes? id "/"))

(defn ids
  "Every app el-id on the page, as the strings the DOM carries."
  []
  (bind!)
  (let [all-ids
        (.evaluate (sdk/page)
                   "() => Array.from(document.querySelectorAll('[id],[data-testid]'))
                           .map(e => e.dataset.testid || e.id)")]
    (vec (sort (filter app-el-id? all-ids)))))

(defn scan
  "Nested map of every app el-id on the page with its value or text."
  []
  (bind!)
  (values/scan {:ignore (complement app-el-id?)}))

(defn shot!
  "Screenshot of the page into target/explore/<name>.png; returns the path."
  [shot-name]
  (bind!)
  (let [file
        (io/file "target/explore" (str shot-name ".png"))]
    (io/make-parents file)
    (.screenshot (sdk/page) (.setPath (Page$ScreenshotOptions.) (.toPath file)))
    (.getPath file)))
