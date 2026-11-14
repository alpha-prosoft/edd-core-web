# edd-core-web-playwright


A thin Clojure wrapper around the **Playwright Java SDK**.  Every test
automatically produces a Playwright Trace Viewer recording — no JUnit Platform,
no extra reporting setup.

## Why this exists

Raw Playwright Java interop works, but repeating browser/context/tracing
lifecycle in every test is noise.  This library provides three macros that
handle that lifecycle so test authors can focus on assertions:

- `with-ui` — start browser, enable tracing, run body, save trace, close browser
- `with-api` — provide an `APIRequestContext`; attach to an existing trace or
  produce a standalone one
- `step` — annotate a logical block in the trace viewer with a name and timing

That is the entire abstraction.  Everything else is direct Playwright interop.

## Quick start

Install Playwright browsers (once):
```bash
npx playwright install chromium
```

Run the sample tests:
```bash
clojure -M:test
```

Expected: 6 tests, 1 intentional failure (`failing-step-demo`).

## Writing tests

```clojure
(ns my-app.tests
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [testing.pw :refer [with-ui with-api step pw-screenshot *current-page*]]))

(deftest homepage-test
  (with-ui [page]

    (step "Navigate to example.com"
      (fn []
        (.navigate page "https://example.com")))

    (step "Verify heading"
      (fn []
        (is (= "Example Domain"
               (.textContent (.locator *current-page* "h1"))))))))
```

The `with-ui` binding accepts an optional options map:

```clojure
(with-ui [page {:headless false :output-dir "target/my-traces"}]
  ...)
```

| Option | Default | Description |
|--------|---------|-------------|
| `:headless` | `true` | Show a browser window when `false` |
| `:output-dir` | `"target/pw-traces"` | Where trace zips are written |
| `:slow-mo` | `0` (headed: `80`) | Milliseconds between actions |

To run in headed mode without changing test code, pass the JVM property on the
command line:

```bash
clojure -M:test -J-Dplaywright.browser.headless=false
```

## REST testing with `with-api`

`with-api` provides a Playwright `APIRequestContext` bound to `client`.

**Attached** — nested inside `with-ui`, HTTP calls appear in the same trace:

```clojure
(with-ui [page]
  (step "Navigate"
    #(.navigate page "https://example.com"))

  (step "REST call in same trace"
    #(with-api [client]
       (let [resp (.get client "https://api.example.com/status" nil)]
         (is (= 200 (.status resp)))))))
```

**Standalone** — no browser required, produces its own `trace.zip`:

```clojure
(deftest api-test
  (with-api [client]
    (let [resp (.get client "https://jsonplaceholder.typicode.com/posts/1" nil)]
      (is (= 200 (.status resp))))))
```

## API reference

### `with-ui`

Starts Chromium, enables full tracing (screenshots + DOM snapshots + sources),
runs the body, saves `<test-name>-<timestamp>.zip`, closes the browser.

Binds the `Page` to the name you choose and also to the dynamic var
`*current-page*` so helpers called from inside the body do not need `page`
threaded through as an argument.

(`with-pw` is a deprecated alias — delegates to `with-ui`.)

### `with-api`

Provides a Playwright `APIRequestContext` bound to `client`.

- Inside `with-ui`: `BrowserContext.request()` returns the context's
  `APIRequestContext` directly (it is **not** a factory — do not call
  `.newContext` on it).
- Standalone: uses `Playwright.request().newContext(opts)` to create the
  client, starts/stops a tracing context around the body.

Accepts `:ignore-https-errors true` as an option.

### `step`

Wraps a zero-arg function in a named Playwright trace group.  Reads
`*current-page*` automatically.

```clojure
(step "Fill login form"
  (fn []
    (.fill *current-page* "#email" "user@example.com")
    (.click *current-page* "button[type=submit]")))
```

### `pw-screenshot`

Captures a labelled screenshot inside the current trace context.  Reads
`*current-page*`.  Use this for explicit named snapshots; the trace's
automatic film-strip is usually sufficient.

```clojure
(pw-screenshot "after-submit")
```

### `start-browser` / `stop-browser`

Low-level helpers used internally.  `start-browser` returns
`{:playwright :browser :context :page :trace-path}`.

## Aliases

| Alias | Command | What it does |
|---|---|---|
| `:test` | `clojure -M:test` | Run tests via Kaocha |
| `:aggregate` | `clojure -M:aggregate` | Copy trace viewer assets + generate `index.html` from existing zips |
| `:traces` | `clojure -M:traces` | Serve the report over HTTP at `http://localhost:8080` |
| `:nrepl` | `clojure -M:nrepl [port & namespaces]` | Start a browser + nREPL (port 7888), then require the namespaces |

## Interactive REPL

```bash
clojure -M:nrepl
# nREPL on port 7888; Chromium opens immediately (-J-Dplaywright.browser.headless=true for none)
```

```clojure
(require '[testing.repl :refer [page restart stop]])

(.navigate @page "https://example.com")
(.textContent (.locator @page "h1"))  ;; => "Example Domain"

(restart)  ;; fresh browser
(stop)     ;; close everything
```

## Viewing traces

Each test writes `target/pw-traces/<name>-<timestamp>.zip`.

**Via the built-in HTTP server** (serves the full Trace Viewer UI):

```bash
clojure -M:traces
```

Then open `http://localhost:8080/index.html` in your browser.  The port and
serve directory can be overridden:

```bash
clojure -M:traces -J-Dpw.server.port=9090 -J-Dpw.server.dir=target/pw-traces
```

**Via the Playwright CLI** (requires Node/npx):

```bash
npx playwright show-trace target/pw-traces/my-test-1234567890.zip
```

Or drag and drop the zip to https://trace.playwright.dev

## License

[Apache 2.0](../../LICENSE), see [NOTICE](../../NOTICE).
