#!/bin/bash
# Release version as cbd-jenkins-pipeline computes it (repo.resolveVersion):
# {project.json version}.{commit count}.0
set -euo pipefail

if [ "$(git rev-parse --is-shallow-repository)" = "true" ]; then
  echo "Shallow clone: commit count would be wrong, fetch full history" >&2
  exit 1
fi

major=$(python3 -c 'import json; print(json.load(open("project.json")).get("version", 1))' 2>/dev/null || echo 1)
commits=$(git rev-list --count HEAD)

echo "${major}.${commits}.0"
