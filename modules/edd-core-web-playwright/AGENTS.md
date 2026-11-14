# Agent Development Guide — edd-core-web-playwright

This document is a reference for AI agents (and developers) working on the
`edd-core-web-playwright` module itself (namespaces `testing.*`).

---

## 1. What this library is

`edd-core-web-playwright` (formerly clojure-playwright) is a thin Clojure wrapper around the **Playwright Java SDK**.
It exists because:

- Raw Playwright interop is verbose — browser/context/tracing lifecycle is
  repeated in every test.
- We want every test to automatically produce a Playwright Trace Viewer recording
  without any per-test boilerplate.

The library is intentionally small.  It is **not** a framework — it is a few
macros and helpers that handle the lifecycle so test authors can focus on
assertions.

---

## 2. Repository Layout

```
modules/edd-core-web-playwright/
├── deps.edn                    # library + aliases (:test, :nrepl, :aggregate, :traces)
├── tests.edn                   # Kaocha config for :test alias
├── src/
│   └── testing/
│       ├── pw.clj              # Primary public API (with-ui, with-api, step, …)
│       ├── pw_report.clj       # Aggregate HTML report generator
│       ├── pw_http_server.clj  # Pure-Java HTTP server for trace viewer
│       └── repl.clj            # REPL helpers (page atom, restart, stop)
└── test/
    └── samples/
        └── pw_demo_test.clj    # Sample / smoke tests for the library itself
```

---

## 3. Public API (`testing.pw`)

All public symbols live in `src/testing/pw.clj`.

### `with-ui`

Main test macro.  Manages browser + tracing lifecycle.  Binds the Playwright
`Page` both lexically (to the name you choose) and dynamically to
`*current-page*`.

```clojure
(with-ui [page]
  (step "Navigate" #(.navigate page "https://example.com"))
  (step "Assert"   #(is (= "Example Domain" (.textContent (.locator page "h1"))))))

;; with options
(with-ui [page {:headless false :output-dir "target/my-traces"}]
  ...)
```

Options: `:headless` (default `true`), `:output-dir` (default `"target/pw-traces"`),
`:slow-mo` (default `0`, or `80` when headed).

`with-pw` is a **deprecated alias** — keep it for back-compat but do not use
it in new code.

### `with-api`

Provides a Playwright `APIRequestContext` bound to `client`.

- **Attached** (inside `with-ui`): derives client from `BrowserContext.request()` —
  HTTP calls appear in the same trace as browser actions.
- **Standalone**: starts a fresh headless browser + tracing context, produces its
  own `trace.zip`, then cleans everything up.

```clojure
;; Standalone
(with-api [client]
  (let [resp (.get client "https://api.example.com/status" nil)]
    (is (= 200 (.status resp)))))

;; Inside with-ui (attached, same trace)
(with-ui [page]
  (step "REST call"
    #(with-api [client]
       (is (= 200 (.status (.get client url nil)))))))
```

Important implementation note: `BrowserContext.request()` returns an
`APIRequestContext` **directly** — it is NOT a factory and `.newContext` must
not be called on it.  Only `Playwright.request()` is the factory.

### `step`

Wraps a thunk in a named Playwright trace group.  Reads `*current-page*`
automatically.

```clojure
(step "Fill login form"
  (fn []
    (.fill *current-page* "#email" "user@example.com")
    (.click *current-page* "button[type=submit]")))
```

### `pw-screenshot`

Captures a labelled screenshot and logs it.  Reads `*current-page*`.

```clojure
(pw-screenshot "after-submit")
```

### `start-browser` / `stop-browser`

Low-level helpers used internally by `with-ui` and `with-api`.  Exposed
publicly for advanced use.  `start-browser` returns a map of
`{:playwright :browser :context :page :trace-path}`.

### Dynamic vars

| Var | Bound by | Purpose |
|---|---|---|
| `*current-page*` | `with-ui`, `with-api` (standalone) | Current Playwright `Page` |
| `*current-api*` | `with-api` | Current `APIRequestContext` |

---

## 4. Sample Tests (`test/samples/pw_demo_test.clj`)

The sample tests double as smoke tests for the library.

| Test | What it verifies |
|---|---|
| `example-dot-com-test` | Basic navigation, title/heading assertions, explicit screenshot |
| `playwright-website-test` | Multi-step test with options map (`output-dir`) |
| `failing-step-demo` | **Intentionally fails** — verifies trace captures failure state |
| `with-api-standalone-test` | `with-api` with no active browser; GET + POST |
| `with-api-attached-test` | `with-api` nested inside `with-ui`; same trace |

Run:
```bash
clojure -M:test
```

Expected result: 6 tests, 1 failure (`failing-step-demo` — intentional).

---

## 5. Interactive REPL

```bash
clojure -M:nrepl
# nREPL on port 7888; Chromium opens immediately (-J-Dplaywright.browser.headless=true for none)
clojure -M:nrepl 7888 my-app.explore   # also requires the app's explore helpers
```

```clojure
(require '[testing.repl :refer [page restart stop]])

(.navigate @page "https://example.com")
(.textContent (.locator @page "h1"))   ;; => "Example Domain"

(restart)  ; fresh browser
(stop)     ; close everything
```

---

## 6. Aliases

| Alias | Command | What it does |
|---|---|---|
| `:test` | `clojure -M:test` | Run tests via Kaocha |
| `:aggregate` | `clojure -M:aggregate` | Copy trace viewer assets + generate `index.html` from existing zips |
| `:traces` | `clojure -M:traces` | Serve the report over HTTP at `http://localhost:8080` |
| `:nrepl` | `clojure -M:nrepl [port & namespaces]` | Start a browser + nREPL (port 7888), then require the namespaces |

---

## 7. Design Constraints

- **No JUnit dependency** — this is Playwright on the JVM only.
- **No new framework** — keep the API surface minimal.  If something can be done
  with raw Playwright interop in 1-2 lines, do not abstract it.
- **Every test gets a trace** — `with-ui` and `with-api` (standalone) always
  produce a `target/pw-traces/<name>-<ts>.zip`.
- **LSP false positives are expected** — clj-kondo / clojure-lsp cannot resolve
  macro-introduced bindings (`page`, `client`, `*current-page*` inside body
  forms).  These are cosmetic and do not affect compilation or test execution.
- **`with-pw` is a deprecated alias** — keep it for back-compat; it just
  delegates to `with-ui`.

---

## 8. Making Changes

1. Edit `src/testing/pw.clj`.
2. Run `clojure -M:test` — expect 6 tests, 1 intentional failure.
3. If you bump Playwright, install its browser (section 11).
4. If you add a new public symbol, add a sample test in `pw_demo_test.clj`
   and document it in this file and in `README.md`.

---

## 9. Testing an app with this library

How downstream projects use it (`web-samurai`, `opencode-manager/web`).

### Wiring

```clojure
;; deps.edn of the app
:it {:extra-paths ["test/it"]
     :extra-deps  {com.alpha-prosoft/edd-core-web-playwright {:mvn/version "<edd-core-web version>"}}
     :main-opts   ["-m" "kaocha.runner" ":it"]}
:headed {:jvm-opts ["-Dplaywright.browser.headless=false"]}
```

Tests live in `test/it/<app>/*_it.clj`, one `deftest` per flow, one
`with-ui` per `deftest`, and every logical action wrapped in a `step` so the
trace reads like the test.  App-specific helpers (`login`, `goto`,
`wait-visible`, `click`, `fill`) live in a `helpers.clj` next to the tests and
read `*current-page*`, so they never take a page argument.

```clojure
(deftest runs-it
  (with-ui [_page]
    (step "Runs can be filtered"
          (fn []
            (h/login (:member h/fixture))
            (h/goto "/runs")
            (h/click els/filter-select)
            (h/click (els/filter-option :active))
            (is (= "active" (h/input-value els/filter-value)))))))
```

### Element ids (`el-id`) — the only way to locate elements

Never locate by visible text, CSS classes, MUI classes, tag structure or
xpath.  If a test needs an element that has no id, add one to the view.

Each view namespace has a sibling `elements.cljc` that defines the ids.  It is
`.cljc` so the ClojureScript view and the JVM test call **the same function**
and can never drift apart:

```clojure
;; src/.../events/elements.cljc
(ns samurai.events.elements (:require [samurai.el :refer [el-id]]))
(defn publish-button [event-name] (el-id ::publish-button event-name))
;; => ":samurai.events.elements/publish-button--samurai-spring-cup-2027"

;; view (cljs)
[:> Button {:id (ev-els/publish-button event-name)} "Publish"]

;; test (clj)
(.click (.locator page (el-id-sel ev-els/publish-button event-name)))
```

The suffix is sanitised (lower-cased, `+` → `plus`, whitespace → `-`, other
non-alphanumerics dropped) and joined with `--`, so row-specific ids can be
built from data the test already knows (an event name, a user name).

Two flavours exist:

| Project | id helper | test selector |
|---|---|---|
| web-samurai | `samurai.el/el-id` (namespaced keyword + optional suffix) | `el-id-sel` → `[id='…']`, `testid-sel` → `[data-testid='…']`, `testid-prefix-sel` → every id made by one fn |
| opencode-manager | `web.primitives.testing.el-id/el-id` from `edd-web-primitives` (keyword, or vector path `[:runs :filter-option :active]`) | `manager-web.el/sel` → matches `[id='…'],[data-testid='…']` |

Rules that apply in both:

- edd-web-primitives components take `:el-id` directly; MUI/hiccup elements get
  `:data-testid (el/id …)`, or `:id` where MUI forwards it to the input
  (TextField).
- ARIA roles (`[role='dialog']`) are fine for scoping, never combined with text.
- Reading the text of an id'd element to assert on it is fine.
- Only third-party pages are exempt (e.g. Keycloak's `#username`/`#kc-login`,
  the edd login widget).

### Waiting

Wait on a positive signal (`.waitFor` on an id'd locator, `NETWORKIDLE`),
not `Thread/sleep`.  Poll with a timeout when the backend is eventually
consistent.

### Debugging a failing test

Open the trace (`npx playwright show-trace target/pw-traces/<name>-FAIL-<ts>.zip`),
or connect to the app's Playwright nREPL and replay the steps one by one
against `@page`.

---

## 10. Generating user documentation from tests

Both apps generate their end-user guides by **driving the real UI with this
library** and taking annotated screenshots.  The guide text lives in the
guide code, never in the generated files, so screenshots and text cannot go
out of date relative to the app.

| Project | Code | Output | Run |
|---|---|---|---|
| web-samurai | `test/guide/samurai/*_guide.clj` | static HTML in `www/guide` (+ copy in `target/user-guide`) | `clj -M:user-guide` / `make user-guide` |
| opencode-manager | `web/test/guide/manager_web/*_guide.clj` | MkDocs Markdown in `docs/guide` | `cd web && clojure -M:guide [member operator …]` / `make guide` |

Both are built from the same three pieces:

1. **Block DSL** (`guide-doc`) — functions that append blocks to a dynamic
   `*blocks*` atom while the browser runs:
   `chapter!`, `section!`, `p!`, `steps!`, `tip!`, `note!`, `figure!`
   (opencode-manager adds `bullets!`, `table!`, and `slot!`/`fill!` to place
   output of slow runs where it belongs).  Text is Markdown-ish: UI labels
   in `**bold**`, links as `[text](page#anchor)`.
2. **Annotated screenshots** (`guide-shots/shot!`) — injects an overlay into
   the page that outlines targets and draws labelled arrows, crops around a
   context element, screenshots, then removes the overlay.  Targets are the
   same el-id selectors the tests use.
3. **Renderer** — after the run, numbers figures per chapter, builds a TOC and
   writes the pages.  Rendering happens only if the guide completed; on
   failure a `FAILED*.png` of the page is saved and the published guide is
   left untouched.

```clojure
(defn- events-page []
  (chapter! :events "1. The events page"
            "Every tournament starts as an **event**.")
  (h/navigate-to-events)
  (figure! "e01-events-page"
           "The events page. Archived events are hidden until you tick **Show archived**."
           {:full  true
            :marks [{:target (el-id-sel ev-els/show-archived-checkbox)
                     :label "Include archived events" :side :bottom :color :purple}
                    {:target (testid-sel ev-els/add-event-button)
                     :label "Create a new event" :side :bottom :color :green}]}))

(deftest user-guides
  (doc/define-guide! :events {:title "Events Guide" :file "events.html"})
  (with-ui [page]
    (.setViewportSize page 1440 1000)
    (doc/in-guide :events (step "Events guide" events-page)))
  (doc/render-all! [:events]))
```

`shot!` options: `:marks` (selector, locator, or
`{:target :label :side :color :distance}`), `:ctx` (element the crop is
built around), `:pad`, `:full` (whole viewport), `:spotlight` (dim all but
`:ctx`).

Making guides reproducible:

- Use fixed data (fixed names, dates, ids; opencode-manager seeds a scripted
  LLM stack so agent output is identical on every run).
- Clean up leftovers from interrupted runs before starting, and what the
  guide created after it finishes.
- Shoot each guide as a user with exactly the role the guide is for.
- Fix viewport, device scale factor and colour scheme.

---

## 11. Upgrading Playwright

A new `com.microsoft.playwright/playwright` version needs its own browser
build (e.g. 1.65 → `chromium_headless_shell-1254`).  `start-browser` sets
`PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`, so install it once:

```bash
clojure -M -e '(com.microsoft.playwright.CLI/main (into-array String ["install" "chromium"]))'
```

Every Playwright install garbage-collects browser builds in
`~/.cache/ms-playwright` that no *live* driver links to, and the Java driver
links from a temp dir that disappears on exit.  So an install from one
project (or a concurrently running test suite on another version) can delete
another project's browsers.  If tests fail with `Executable doesn't exist`,
reinstall for that version; installing via `npx playwright@<version> install
chromium` keeps a persistent link and survives the cleanup.
