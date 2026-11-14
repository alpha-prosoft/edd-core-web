(ns samples.pw-aggregate-test
  "Demonstrates how to aggregate Playwright traces into an HTML report.

   This namespace shows two patterns:

   Pattern A — call generate-report programmatically after tests run.
   Pattern B — use the :pw-report CLI alias from deps.edn.

   Run the pw-demo tests first so traces exist:
     clojure -M:pw-test

   Then generate the aggregate HTML report:
     clojure -M:pw-report

   Or do both in one shot:
     clojure -M:pw-test && clojure -M:pw-report

   The report is written to target/pw-traces/index.html.
   Open it in a browser:
     open target/pw-traces/index.html
     # or
     python3 -m http.server 8080 --directory target/pw-traces
     # then open http://localhost:8080 and use the online viewer links.
  "
   (:require
    [clojure.java.io :as io]
    [clojure.string  :as str]
    [clojure.test    :refer [deftest is testing]]
    [testing.pw      :refer [step with-pw]]
    [testing.pw-report :refer [generate-report]]))

;; ---------------------------------------------------------------------------
;; Pattern A — inline report generation inside a test suite fixture
;; ---------------------------------------------------------------------------

(defonce ^:private traces-dir "target/pw-traces")

(defn suite-fixture
  "Use as a :once fixture to auto-generate the aggregate report after
   all tests in this namespace have run.

   Add to your test ns:
     (use-fixtures :once suite-fixture)
  "
  [run-tests]
  (run-tests)
  ;; After the full suite, emit the aggregate HTML dashboard.
  (when (.exists (io/file traces-dir))
    (generate-report {:traces-dir traces-dir
                      :output-dir traces-dir
                      :project    "Playwright Demo Suite"})))

;; Activate the fixture for this namespace's tests.
(clojure.test/use-fixtures :once suite-fixture)

;; ---------------------------------------------------------------------------
;; A simple test so this namespace has something runnable
;; ---------------------------------------------------------------------------

(deftest aggregate-report-test
  "Runs a quick browser test, then generates the aggregate HTML report.
   This test is self-contained and can be run standalone."
  (with-pw [page {:output-dir traces-dir}]

    (step "Navigate to example.com"
      (fn []
        (.navigate page "https://example.com")))

    (step "Verify title"
      (fn []
        (testing "page title includes Example"
          (is (str/includes? (.title page) "Example"))))))

  ;; After the test body finishes the trace.zip has been written.
  ;; Now generate/refresh the aggregate report.
  (let [report-file (generate-report {:traces-dir traces-dir
                                      :output-dir traces-dir
                                      :project    "Playwright Demo Suite"})]
    (testing "aggregate report file exists"
      (is (.exists report-file)
          "generate-report should create index.html"))
    (testing "report is non-empty"
      (is (pos? (.length report-file))
          "index.html should have content"))))

;; ---------------------------------------------------------------------------
;; Pattern B — programmatic aggregation of an external traces directory
;; ---------------------------------------------------------------------------

(defn aggregate-external-dir
  "Utility fn: scan any directory you point at and generate a report.

   Example:
     (aggregate-external-dir \"/ci/artifacts/traces\" \"My CI Run\")
  "
  [dir project-name]
  (generate-report {:traces-dir dir
                    :output-dir dir
                    :project    project-name}))

;; Example invocation (not a test, just illustrative):
;; (comment
;;   (aggregate-external-dir "target/pw-traces" "Regression Suite v1.2.3"))
