#!/bin/bash
# Run by the pipeline before the docker build.
set -euo pipefail

./changes.py --shallow --check

version=$(./version.sh)
coordinate="com.alpha-prosoft/edd-core-web {:mvn/version \"${version}\"}"
if ! grep -qF "${coordinate}" README.md; then
  echo "README.md must show the release of this commit: ${coordinate}" >&2
  exit 1
fi
echo "README.md shows version ${version}"
