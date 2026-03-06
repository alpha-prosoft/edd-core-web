(ns web.primitives.data-display.divider-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.primitives.data-display.core :refer [RawDivider]]
            [reagent.core :as r]))

(deftest raw-divider-renders
  (testing "RawDivider renders without crashing"
    (is (some? (r/as-element [RawDivider {}])))))
