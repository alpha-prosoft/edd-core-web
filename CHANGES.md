# Changelog

## Changes

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
