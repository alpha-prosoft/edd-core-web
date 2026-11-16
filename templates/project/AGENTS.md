# projectname

re-frame app on [edd-core-web](https://github.com/alpha-prosoft/edd-core-web).
UI comes from edd-web-primitives (MUI) and edd-web-widgets. UI tests, the
user guide and exploring the app use edd-core-web-playwright (browser,
traces, nREPL; namespaces `testing.*`) with the component SDK from
edd-web-primitives-ui-sdk (`web.primitives.sdk.*`). All are released together
under one version: bump them together.

## Running

```sh
clojure -M:shadow-cljs:dev:test:run     # https://projectname.local.localdevhub.com:3000
clojure -M:it                           # Playwright tests against the running app
clojure -M:it:headed                    # same, with a visible browser
clojure -M:guide                        # regenerate docs/guide from the running app
clojure -M:explore                      # nREPL on 7888 with a browser, see below
clojure -M:format fix
```

Tests, the guide and the explorer run against `-Dapp.url` (default the URL
above), so the app must be running. Don't start a second shadow-cljs if one
is already running; read its output for build errors instead.

## Using edd-core-web

- `projectname.core/init` calls `edd.core/init` with `:pages`, `:routes`,
  `:menu`, `:translations`, `:theme`, `:body`. Add a page there; never mount
  components or register routes by hand.
- A page is `projectname.<page>.{core,db,events,subs,views,elements}`. `core/main`
  returns `{:init ::events/init :panel views/main-panel}`.
- Navigate with `[::edd.events/navigate :page {:id id}]` or
  `[::edd.events/navigate {:page :about :path {...} :query {...}}]`; never set
  `js/location`.
- Backend calls go through `::edd.client/call` (`:service`, `:query` |
  `:command` | `:commands`, `:on-success`, `:on-failure`), `::edd.client/call-n`
  for parallel calls and `::edd.client/deps` for dependent queries. Never call
  `js/fetch`.
- A button that triggers a call is disabled and shows a spinner while it
  runs; reset the flag in both the success and the failure handler.
- Text goes through `edd.i18n/tr` with keys in `projectname.i18n`, for every
  language; no literal UI strings in views.

## UI components and styling

- Use `web.primitives.components` (`RawButton`, `RawGrid`, `RawTypography`,
  ...) and widgets (`web.widgets.*`: confirmation dialog, snackbar, upload,
  translation, `folder` pictures kept by a storage service, `lineage`
  history of a thing) before MUI directly. Use MUI (`[:> Component ...]`) only for
  what primitives don't wrap.
- Style with theme tokens (`"primary.main"`, `"text.secondary"`) and the
  `:theme` passed to `init`; never hex colors or ad-hoc sizes. Add a token to
  the theme instead of inlining a style.

## Element ids (el-id)

Every element a test or guide touches has an el-id, defined once in the
page's `elements.cljc` and used by both the view and the test:

```clojure
(ns projectname.home.elements)

(def increment ::increment)

(defn item [id] [::item id])
```

- Primitives take it as `:el-id`; on plain hiccup or MUI elements use
  `:data-testid (web.primitives.testing.el-id/el-id el/x)`.
- Tests and guides pass the same value to the SDK. Never select by text, CSS
  class or DOM structure; if an element has no el-id, add one.
- The DOM carries the resolved string: `el/increment` becomes
  `projectname.home.elements/increment`, `(el/item 1)` becomes
  `projectname.home.elements/item--1` (suffixes lower-cased, joined with
  `--`). `sdk/el` and `sdk/by-el-id` take the keyword or vector, never the
  string.

## Exploring the app (nREPL)

Use the explorer to see what a page renders, find el-ids and try a test's
steps one by one before writing them down, or to find why a test fails.

```sh
clojure -M:explore                                   # browser window + nREPL on 7888
clojure -J-Dplaywright.browser.headless=true -M:explore   # no window
```

It starts a browser (`testing.repl/page`, an atom holding the Playwright
`Page`), writes `.nrepl-port` and loads `projectname.explore`. Send forms
with any nREPL client, e.g. `rep` (reads `.nrepl-port`):

```sh
rep "(require '[projectname.explore :as x] '[projectname.home.elements :as el] \
              '[web.primitives.sdk.core :as sdk] '[web.primitives.sdk.button :as button] \
              '[web.primitives.sdk.values :as values])"
rep '(x/open! "/")'                ; navigate, binds the SDK to the browser
rep '(x/ids)'                      ; every el-id on the page
rep '(x/scan)'                     ; el-ids nested by path, with values and text
rep '(button/click! el/increment)' ; act exactly as a test does
rep '(values/value-of el/clicks)'  ; => "Current count: 1"
rep '(x/shot! "home")'             ; => "target/explore/home.png", open it to look
```

- Use el-ids from the page's `elements.cljc`, the same way tests do. If
  `x/ids` doesn't list the element you need, add an el-id to the view
  (shadow-cljs reloads it) instead of reaching for text or CSS selectors.
- After `(testing.repl/restart)` call `x/open!` again; it rebinds the SDK.
- Raw Playwright is available on `@testing.repl/page` for anything the SDK
  doesn't cover (`.evaluate`, `.waitForLoadState`, console messages).
- Once a sequence works, copy it into a `step` of an `*_it.clj` test.

## Tests

- Unit: events and subs as plain functions under `test/`.
- UI: `test/it/projectname/*_it.clj`, run by kaocha (`tests-it.edn`). Wrap a
  test in `h/with-app`, group actions with `testing.pw/step`, and act through
  `web.primitives.sdk.*` (`button/click!`, `text-field/fill!`,
  `display/text=?`, `sdk/eventually`). Read-after-act assertions use the
  waiting helpers, never `Thread/sleep`. Each run writes a Playwright trace
  to `target/pw-traces`; open a failing one with
  `npx playwright show-trace target/pw-traces/<test>-FAIL-<ts>.zip`, or replay
  the steps in the explorer.
- Test real flows (create, edit, navigate, error), not only rendering.

## Documentation

The user guide in `docs/guide` is generated, not written by hand. Each guide
is code in `test/guide/projectname/*_guide.clj` that drives the running app
through the same SDK and el-ids as the tests and adds blocks with the DSL
in `projectname.guide-doc`:

```clojure
(chapter! "Count clicks" "The home page counts how often you select **Increment**.")
(steps! "Open **Home**." "Select **Increment**.")
(h/open! "/" el/title)
(button/click! el/increment)
(figure! "home-01-increment"
         "**Home** after one click on **Increment**."
         {:marks [{:target el/increment :label "Counts a click" :color :blue}]})
```

- `figure!` screenshots the page (or the element `:ctx`) and outlines and
  labels each `:marks` target. Image names start with the guide's prefix and
  a number.
- A guide is written only when it completes, so a broken flow never
  publishes a half guide. Register new guides in `projectname.guide/guides`.
- Change guide text in the guide code, never in `docs/guide/*.md`. When a
  feature or a label on screen changes, update its guide and run
  `clojure -M:guide`; commit the regenerated Markdown and images.
- UI labels in **bold**, written as they appear on screen. Describe what the
  user does and sees, not how it is implemented.

Don't over-document: README covers running the project, this file covers how
to work on it.

# Clojure conventions

## `let` binding formatting

Binding name on its own line, value on the next, a blank line between pairs.

```clojure
(let [role
      (get-in ctx [:meta :user :role])

      status
      (get-in ctx [:application :attrs :status])]
  (do-something role status))
```

## Prefer `let` over threading, `get-in` over `->`

```clojure
;; good
(let [with-cmds
      (update-in ctx [:edd-core :commands] apply-cmd-rules)]
  (get-in with-cmds [:meta :user :role]))

;; bad
(-> ctx (update-in [:edd-core :commands] apply-cmd-rules) :meta :user :role)
```

## Tests

A new line after `(is`, between expected and actual, and after each `(is ...)`:

```clojure
(is
 (= "Current count: 2"
    (display/text=? el/clicks "Current count: 2")))
```

## Comments

Only where the code can't say it; prefer clearer names and structure.
