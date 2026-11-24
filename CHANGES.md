# Changelog

## Changes

### Extend client POST timeout to 121 seconds

Slow queries and commands can exceed the old 31-second deadline.
Give the server more time to respond before the client retries.

- allow 121 seconds for each default POST attempt
- distinguish a deadline from cancellation with TimeoutError
- leave configured overrides and retry backoff unchanged

**[AP-1134]** Client: requests time out, so a stalled one is retried
- edd.client/fetch turns the :timeout of the request params (31 s for
  commands and queries, 50 s for loads) into an AbortSignal; window.fetch
  has no timeout, so a request on a connection that stalled (network
  change, sleep) never settled and its retry never ran
- the aborted request rejects into the existing retry with backoff; a
  repeated command carries the same request id, so edd answers it from
  the stored response

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AP-1128]** Folder pictures: private images via signed urls, public path unchanged
- an entry may set :public false; the image is uploaded to a private folder
  and shown through the storage service's short-lived signed GET url, read
  from :folder->get (query id overridable with :get-query-id) and refreshed
  before it expires; the field keeps the stable file ref, not the signed url
- public entries are unchanged: their immutable CDN url, no extra query or
  polling
- private entries keep the image type and magic-byte guard; gallery demo

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AP-1126]** Folder crop: ship the stylesheet self-contained, guard before the dialog
- embed the react-image-crop stylesheet, scoped under a crop class and
  injected into the document head on mount, so the crop rectangle, handles
  and overlay render wherever the widget is used, with no host change
- run the image type and magic-byte check before opening the crop dialog,
  so a non-picture is refused up front
- the gallery test now asserts the crop selection is positioned over the
  image, so a missing stylesheet fails the build

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AP-1127]** Folder pictures: Content-MD5 on every upload
- The widget reads the chosen file, computes its MD5 (goog.crypt.Md5) and
  asks :folder->upload-url with :md5, so the storage service signs it
- The PUT sends Content-MD5 (put-headers adds it when the storage service
  did not list it): S3 buckets with Object Lock refuse a PUT without
  Content-MD5 or a checksum header

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AP-1123]** Folder pictures: crop before upload
- a new web.widgets.folder.crop ns (canvas + react-image-crop), ported
  from web-samurai, generic with no app deps
- an entry turns cropping on with :crop (true or {:aspect r}) via
  images-spec or a prop; without it the widget is unchanged
- a cropped file still runs the type and first-byte check before upload
- adds react-image-crop 11.0.7; gallery variant and a browser test

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AP-1105]** Folder pictures: only pictures, named messages, saves wait, order
- :accept (default JPEG, PNG, WebP, GIF), checked by type and first bytes
- Every upload message names its file
- ::events/when-uploaded, ::subs/uploading, "Uploading 2 pictures…"
- Chosen order kept; ImageList :on-reorder moves pictures earlier/later
- 40 px tile buttons; PUT sends the signed headers; :put-fx
- Gallery page and browser tests

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AL-39]** Folder and lineage widgets
- web.widgets.folder: pictures kept in a folder of a storage service;
  ImageList (a list entry, uploads add) and ImageField (one picture, an
  upload replaces it) upload through the application's call effect and
  hand on the public URL; ::save sets a folder up, ::retain keeps only
  what the thing it belongs to uses after it is saved
- web.widgets.lineage: the history of a thing from a lineage service,
  moved from allora's lineage-web

**[AP-1088]** Login widget: hidden openers, one live region, retries, region
- An opener hidden by visibility, opacity, no size or an inert/aria-hidden
  ancestor counts as gone, so :focus-fallback gets the focus
- Notices in one aria-live region of the dialog; :announce gives them to
  the app's announcer instead
- The focus starts in the password when the email is filled in
- A request that does not reach Cognito is sent again after 1 s and 3 s
  (:retry-delays, [] for none); answers of Cognito are never repeated
- Cognito's region from the user pool id, :region overrides
- edd.core print-features without cljs.pprint

Signed-off-by: Robert Pofuk <robert.pofuk@allora-it.com>

**[AP-1087]** Login widget: real form, no lost keystrokes, neutral reset, plain errors
- Fields keep every keystroke; the email is trimmed and lower-cased only when sent
- A <form>: Enter sends what the fields hold; buttons disabled only while busy
- name/autocomplete, named dialog and password toggle, focus to the field
  of an error, Escape back to the opener (:focus-fallback)
- Remembers the last email that logged in (:remember-email, default on)
- Reset answers every address the same, waits for Cognito, says when
  offline; its Resend code calls ForgotPassword
- Cognito refusals in plain words (en, de, ru, ua); "Log in", "Create an
  account", "Email address" (:translations keeps old texts)
- Full screen below 600 px; confirmation names the address; :register-note
- Widgets unit tests and login browser tests run in CI

**[AP-1078]** Use edd-core-web-playwright start-browser in all tests
- start-browser takes :executable-path (PLAYWRIGHT_CHROME), :timeout-ms,
  :viewport, :device-scale-factor and :color-scheme
- primitives IT: remove browser.clj, which replaced start-browser with a
  copy of the old one
- scaffold guide starts its browser with pw/start-browser

**[AP-1071]** Add edd-core-web-playwright module, scaffold AGENTS.md, UI tests, guide and explorer
- modules/edd-core-web-playwright: clojure-playwright moved in (testing.*),
  Playwright 1.64.0, REPL headless option and preloaded namespaces
- primitives and widgets integration tests use the module
- scaffold: AGENTS.md on using the libraries, el-ids, tests, guide
  generation and exploring the app over nREPL
- scaffold: el-ids on home/about, :it tests, :guide (docs/guide),
  :explore (nREPL with a browser)

**[AP-1031]** Remove :classes, fix frame deps and template menu

**[AP-1030]** Clenaup script ans certificates

**[AP-1029]** Improve scafolding docu

**[AP-1025]** Update dependencies, fix console and build warnings
- MUI 9.5.0 (lab 9.0.0-beta.10, x-date-pickers and x-tree-view 9.16.0),
  React 19.3.1, marked, react-dropzone, dompurify, shadow-cljs 3.5.8
- SunEditor 3.4.0 with refreshed vendor files
- Maven deps updated in every module and the project template;
  Playwright stays at 1.64 to match ui-runtime-img
- New primitives: RawTimePicker, RawPopover, RawLinearProgress,
  RawCollapse, RawGlobalStyles, RawCheckboxControl and RawRadio;
  RawSelect takes {:value :label} options; RawIconButton keeps icons
  that bring their own props and supports links
- Text editor no longer throws after save or discard: SunEditor's
  autocomplete plugin ran a debounced input handler after destroy
- Text editor switches modes synchronously, so saving no longer
  recreates the editor right before it unmounts (E_INIT_FAIL)
- No compile warnings left in either gallery release build

**[AP-1024]** Relicense to Apache 2.0 under RBI copyright
- The whole repository is Apache 2.0, the same as edd-web-primitives
  and edd-core; edd-core-web and edd-web-widgets were MIT before
- Raiffeisen Bank International AG holds the copyright; alpha-prosoft
  contributions, including edd-web-widgets, are donated to RBI
- NOTICE credits the imported edd-web-primitives project and says it
  has been modified here
- The RBI CONTRIBUTING, CODE_OF_CONDUCT, SECURITY, CODEOWNERS and
  .github move from edd-web-primitives to the repository root;
  contributions are Apache 2.0 and signed off under the DCO
- changes.py (from edd-core) generates CHANGES.md from commits since
  the import; run it after committing and amend

**[AP-1023]** Port modules to edd-web-primitives
- edd-web-primitives is built, tested and published from this repo and is
  the only module with a deps.cljs; core, widgets, frame and the project
  template depend on it.
- Widgets, frame and the template use primitives instead of MUI directly.
  New primitives: RawThemeProvider, RawDropzone, four icons, and
  RawDialog :title-props.
- Generic Gallery with PrimitivesGallery and WidgetsGallery on top of it;
  the widgets gallery replaces devcards.
- Playwright tests for every widget run in CI next to the primitives suite.
- version.sh gives 2.{commit count}.{tags on commit}; pass it to docker build
  as VERSION. Without it the build keeps 1.{BUILD_ID}.
