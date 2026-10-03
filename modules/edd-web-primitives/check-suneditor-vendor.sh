#!/bin/bash
# Fails when the committed SunEditor vendor files differ from the suneditor
# package pinned in src/main/deps.cljs. Fix with `make suneditor-vendor`.
set -euo pipefail

VENDOR_DIR=src/main/web/primitives/text_editor/vendor
PACKAGE_DIST=node_modules/suneditor/dist
VENDORED_FILES=(suneditor.min.js suneditor.min.css suneditor-contents.min.css)

stale=0

for file in "${VENDORED_FILES[@]}"; do
  if ! cmp -s "$PACKAGE_DIST/$file" "$VENDOR_DIR/$file"; then
    echo "Stale vendored SunEditor file: $VENDOR_DIR/$file"
    stale=1
  fi
done

if [ "$stale" -ne 0 ]; then
  echo "Run 'make suneditor-vendor' and commit the result."
  exit 1
fi

echo "Vendored SunEditor files match node_modules/suneditor."
