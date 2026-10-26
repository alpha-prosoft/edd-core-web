(ns web.gallery.it.runner
  "Entry point for the packaged integration-test uberjar. Requires every
   integration-test namespace and runs them via clojure.test, exiting
   non-zero when anything fails. Filesystem-independent so it works from a
   packaged jar on the ui-runtime image."
  (:gen-class)
  (:require
   [clojure.test :as test]
   [web.gallery.it.date-picker-test]
   [web.gallery.it.display-test]
   [web.gallery.it.docs-test]
   [web.gallery.it.facilities-test]
   [web.gallery.it.inputs-test]
   [web.gallery.it.interactions-test]
   [web.gallery.it.smoke-test]
   [web.gallery.it.values-test]))

(def ^:private test-namespaces
  ['web.gallery.it.date-picker-test
   'web.gallery.it.display-test
   'web.gallery.it.docs-test
   'web.gallery.it.facilities-test
   'web.gallery.it.inputs-test
   'web.gallery.it.interactions-test
   'web.gallery.it.smoke-test
   'web.gallery.it.values-test])

(defn -main
  "Runs the primitives suite, or the namespaces named in `args`."
  [& args]
  (let [namespaces
        (if (seq args)
          (map symbol args)
          test-namespaces)

        _
        (apply require namespaces)

        summary
        (apply test/run-tests namespaces)

        failures
        (+ (:fail summary 0) (:error summary 0))]
    (shutdown-agents)
    (System/exit (if (pos? failures) 1 0))))
