(ns samples.pw-demo-test
  "Sample tests demonstrating the testing.pw library.

   These tests use the `with-ui` macro to get a Playwright Page and
   wrap every logical chunk in `step` blocks so they appear as named
   groups in the Playwright Trace Viewer.

   Run:
     clojure -M:test

   Then open the trace:
     npx playwright show-trace target/pw-traces/<name>.zip
     # or drag-drop to https://trace.playwright.dev
  "
  (:require
   [clojure.string  :as str]
   [clojure.test   :refer [deftest is testing]]
   [testing.pw     :refer [pw-screenshot step with-api with-ui]])
  (:import
   [com.microsoft.playwright.options RequestOptions]))

;; ---------------------------------------------------------------------------
;; Test 1 — basic navigation + assertion
;; ---------------------------------------------------------------------------

(deftest ^{:doc "Navigates to example.com and verifies the page title and heading."} example-dot-com-test
  (with-ui [page]

    (step "Navigate to example.com"
      (fn []
        (.navigate page "https://example.com")
        ;; Playwright auto-waits for load; trace captures the full DOM snapshot
        ))

    (step "Verify page title"
      (fn []
        (let [title (.title page)]
          (is (str/includes? title "Example")
              (str "Expected 'Example' in title, got: " title)))))

    (step "Verify h1 heading"
      (fn []
        (let [h1-text (.textContent (.locator page "h1"))]
          (is (= "Example Domain" h1-text)
              (str "Expected 'Example Domain', got: " h1-text)))))

    (step "Take explicit screenshot"
      (fn []
        ;; pw-screenshot captures bytes and logs the label in stdout.
        ;; The trace's automatic film-strip screenshots are richer than this.
        (pw-screenshot "example-domain-loaded")))))

;; ---------------------------------------------------------------------------
;; Test 2 — multiple steps with sub-assertions
;; ---------------------------------------------------------------------------

(deftest ^{:doc "Visits the Playwright documentation homepage and checks key elements."} playwright-website-test
  (with-ui [page {:output-dir "target/pw-traces"}]

    (step "Open Playwright docs"
      (fn []
        (.navigate page "https://playwright.dev")
        ;; Wait for content to settle — trace records network + DOM automatically
        (Thread/sleep 1500)))

    (step "Check page title contains Playwright"
      (fn []
        (testing "page title"
          (is (str/includes? (.title page) "Playwright")))))

    (step "Check hero heading is visible"
      (fn []
        ;; The Playwright homepage has an h1; use isVisible on the locator
        (let [h1 (.locator page "h1")]
          (is (pos? (.count h1))
              "Expected at least one h1 element on the page"))))

    (step "Log viewport size"
      (fn []
        (let [vp (.viewportSize page)]
          (println (str "  Viewport: " (.width vp) "x" (.height vp))))))))

;; ---------------------------------------------------------------------------
;; Test 3 — intentional failure demo (shows error in trace)
;; ---------------------------------------------------------------------------

(deftest failing-step-demo
  "Demonstrates that a failing assertion is recorded in the trace.
   The trace.zip will show the error in the Errors tab of Trace Viewer.
   Comment out the `is` to make the test pass."
  (with-ui [page]

    (step "Navigate to httpbin"
      (fn []
        (.navigate page "https://httpbin.org/get")))

    (step "Intentional failure — wrong status text"
      (fn []
        ;; This will fail on purpose so the trace captures the failure state.
        ;; Remove or fix the assertion to see a green trace.
        (is (= "This text will never match" (.title page))
            "Demo failure: wrong expected title")))))

;; ---------------------------------------------------------------------------
;; Test 4 — with-api standalone (no browser already running)
;; ---------------------------------------------------------------------------

(deftest with-api-standalone-test
  "Demonstrates with-api used on its own (no with-pw).
   A headless browser context is started purely to enable tracing; no page is
   ever navigated.  The HTTP calls appear in the produced trace.zip."
  (with-api [client]

    ;; GET — retrieve a single post
    (let [resp   (.get client "https://jsonplaceholder.typicode.com/posts/1" nil)
          body   (.text resp)
          status (.status resp)]
      (testing "GET /posts/1"
        (is (= 200 status)
            (str "Expected HTTP 200, got: " status))
        (is (str/includes? body "\"id\"")
            "Response body should contain an id field")
        (is (str/includes? body "\"userId\"")
            "Response body should contain a userId field"))
      (println (str "  GET status: " status))
      (println (str "  GET body snippet: " (subs body 0 (min 120 (count body))))))

    ;; POST — create a new resource
    (let [payload "{\"title\":\"pw-test\",\"body\":\"hello\",\"userId\":1}"
          resp    (.post client
                         "https://jsonplaceholder.typicode.com/posts"
                         (-> (RequestOptions/create)
                             (.setData payload)
                             (.setHeader "Content-Type" "application/json")))
          body    (.text resp)
          status  (.status resp)]
      (testing "POST /posts"
        (is (= 201 status)
            (str "Expected HTTP 201, got: " status))
        (is (str/includes? body "\"id\"")
            "Created resource should have an id"))
      (println (str "  POST status: " status))
      (println (str "  POST body: " body)))))

;; ---------------------------------------------------------------------------
;; Test 5 — with-api nested inside with-pw (attached / traced mode)
;; ---------------------------------------------------------------------------

(deftest with-api-attached-test
  "Demonstrates with-api nested inside with-pw.
   The APIRequestContext is derived from the existing traced BrowserContext,
   so the REST call appears alongside browser actions in the same trace.zip."
  (with-ui [page]

    (step "Navigate to example.com"
      (fn []
        (.navigate page "https://example.com")))

    (step "REST call via with-api inside with-ui"
      (fn []
        (with-api [client]
          (let [resp   (.get client "https://jsonplaceholder.typicode.com/posts/1" nil)
                status (.status resp)
                body   (.text resp)]
            (testing "GET inside with-pw"
              (is (= 200 status)
                  (str "Expected 200, got: " status))
              (is (str/includes? body "\"id\"")
                  "Body should contain id field"))
            (println (str "  Attached REST status: " status))))))))
