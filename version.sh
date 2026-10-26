#!/bin/bash
set -euo pipefail

if [ "$(git rev-parse --is-shallow-repository)" = "true" ]; then
  echo "Shallow clone: commit count would be wrong, fetch full history" >&2
  exit 1
fi

commits=$(git rev-list --count HEAD)
tags=$(git tag --points-at HEAD | wc -l)

echo "2.${commits}.${tags}"
