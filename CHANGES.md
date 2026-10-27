# Changelog

## Changes

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
