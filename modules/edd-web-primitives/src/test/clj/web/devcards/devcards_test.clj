(ns web.devcards.devcards-test
  (:require [clojure.test :refer [deftest]]
            [ui-testkit.core :as c]))

(def default-opts
  {:screenshot-dir "target/devcards"
   :reference-dir "src/test/resources/devcards"
   :build-id :devcards
   :aliases [:dev]
   :metric-threshold 0.001
   :verbose true
   :assert? true})

;(deftest when-devcards-ok
;  (c/run-devcard-tests default-opts))

