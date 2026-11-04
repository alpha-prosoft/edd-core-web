#!/bin/bash
# Run by the pipeline before the docker build.
set -euo pipefail

fail() {
  echo >&2
  echo "pre-build failed: $1" >&2
  echo "Fix with:" >&2
  echo "  ./changes.py" >&2
  echo "  sed -i 's#edd-core-web {:mvn/version \"[0-9.]*\"}#edd-core-web {:mvn/version \"'\$(./version.sh)'\"}#' README.md" >&2
  echo "  git add CHANGES.md README.md && git commit --amend --no-edit" >&2
  echo "  ./pre-build.sh" >&2
  exit 1
}

./changes.py --shallow --check || fail "CHANGES.md does not start with the last commit message."

version=$(./version.sh)
coordinate="com.alpha-prosoft/edd-core-web {:mvn/version \"${version}\"}"
if ! grep -qF "${coordinate}" README.md; then
  found=$(grep -oE 'edd-core-web \{:mvn/version "[^"]*"\}' README.md || echo "none")
  fail "README.md shows ${found}, this commit releases ${version}."
fi
echo "README.md shows version ${version}"
