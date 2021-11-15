(ns web.primitives.inputs.utils-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [web.primitives.inputs.utils :as utils]))

(deftest handle-comma-test
  (is (= "" (utils/handle-comma "")))
  (is (= "1" (utils/handle-comma "1")))
  (is (= "12" (utils/handle-comma "12")))
  (is (= "1." (utils/handle-comma "1.")))
  (is (= "1." (utils/handle-comma "1,")))
  (is (= "1.2" (utils/handle-comma "1,2")))
  (is (= "1.23" (utils/handle-comma "1,23")))
  (is (= "1.23" (utils/handle-comma "1,234")))
  (is (= "1.23" (utils/handle-comma "1,23abcde")))
  (is (= "1.23" (utils/handle-comma "qwertz1,23")))
  (is (= "291" (utils/handle-comma "(#291)&:-)"))))

(run-tests 'web.primitives.inputs.utils-test)