(ns web.primitives.icons.utils-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [web.primitives.icons.utils :as utils]))

(deftest handle-props-test
  (let [props {:style {:padding "0 14px"}}]
    (is (= {:style {:width "24px" :height "24px" :viewbox "0 0 24 24" :version "1.1" :xlmns "http://www.w3.org/2000/svg" :padding "0 14px"}}
           (utils/handle-props props)))))

(run-tests 'web.primitives.icons.utils-test)
