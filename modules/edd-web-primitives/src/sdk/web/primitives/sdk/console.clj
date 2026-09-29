(ns web.primitives.sdk.console
  "Collect browser errors while a test runs, e.g. to fail a smoke test when
   a page throws or logs an error.

     (let [errors (console/watch-errors!)]
       ... drive the UI ...
       (is (empty? (console/page-errors errors))))"
  (:require
   [clojure.string :as str]
   [web.primitives.sdk.core :as sdk])
  (:import
   [java.util.function Consumer]))

(def default-ignored
  "React/MUI development warnings that are logged as console errors but do
   not indicate a broken page."
  ["Warning:"
   "React does not recognize"
   "Invalid DOM property"
   "componentWill"
   "Download the React DevTools"])

(defn watch-errors!
  "Start recording errors on the bound page. Returns a handle for
   `page-errors` / `console-errors`. `ignored` is a list of substrings;
   console errors containing any of them are dropped."
  ([]
   (watch-errors! default-ignored))
  ([ignored]
   (let [page-errors    (atom [])
         console-errors (atom [])
         ignored?       (fn [text] (some #(str/includes? text %) ignored))]
     (.onPageError
      sdk/*page*
      (reify Consumer
        (accept [_ err]
          (swap! page-errors conj (str err)))))
     (.onConsoleMessage
      sdk/*page*
      (reify Consumer
        (accept [_ msg]
          (let [text (.text msg)]
            (when (and (= "error" (.type msg))
                       (not (ignored? text)))
              (swap! console-errors conj text))))))
     {:page page-errors :console console-errors})))

(defn page-errors
  "Uncaught exceptions thrown by the page."
  [handle]
  @(:page handle))

(defn console-errors
  "console.error messages that were not ignored."
  [handle]
  @(:console handle))
