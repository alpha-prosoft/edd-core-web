#!/bin/bash
# Run the packaged Playwright integration suite against the static gallery.
#
# Designed for the ui-runtime image (Playwright image + Java). Browsers
# are preinstalled there, so nothing is downloaded. Set PLAYWRIGHT_CHROME to
# drive a different Chrome binary instead (see web.gallery.it.browser).
#
# Inputs:
#   IT_JAR            integration-test uberjar  (default target/it/it-tests.jar)
#   GALLERY_DIR       static gallery to serve   (default resources/gallery)
#   GALLERY_PORT      port to serve on          (default 3002)
#   PLAYWRIGHT_CHROME optional path to a Chrome binary (default: Playwright's bundled browser)
set -e

IT_JAR="${IT_JAR:-target/it/it-tests.jar}"
GALLERY_DIR="${GALLERY_DIR:-resources/gallery}"
GALLERY_PORT="${GALLERY_PORT:-3002}"
GALLERY_URL="http://localhost:${GALLERY_PORT}"

# Never try to download or validate Playwright's own browser bundle.
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
export PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=1

serve_gallery() {
  echo "Serving ${GALLERY_DIR} on ${GALLERY_URL}"
  (cd "${GALLERY_DIR}" && python3 -m http.server "${GALLERY_PORT}") &
  SERVER_PID=$!
}

wait_for_gallery() {
  echo "Waiting for gallery to become available"
  for _ in $(seq 1 60); do
    if curl -sf -o /dev/null "${GALLERY_URL}/index.html"; then
      echo "Gallery is up"
      return 0
    fi
    sleep 1
  done
  echo "Gallery did not become available on ${GALLERY_URL}"
  return 1
}

stop_gallery() {
  if [ -n "${SERVER_PID:-}" ]; then
    kill "${SERVER_PID}" 2>/dev/null || true
  fi
}

trap stop_gallery EXIT

echo "Using Chrome binary: ${PLAYWRIGHT_CHROME:-playwright bundled}"

serve_gallery
wait_for_gallery

echo "Running integration tests against ${GALLERY_URL}"
java -Dgallery.base.url="${GALLERY_URL}" -jar "${IT_JAR}"

echo "Integration tests passed"
