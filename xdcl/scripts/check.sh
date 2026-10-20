#!/usr/bin/env bash
# Checks the whole XDCL workspace:
#   1. syncs the umbrella build, so the language server sees every build's schemas,
#   2. validates every .xdsl / .xdcl file in the workspace (no Gradle involved),
#   3. runs the hello-world app and checks its output.
#
# Uses the XDCL toolkit from XDCL_TOOLKIT (default: ~/xdcl/xdcl-toolkit-<version>, see xdcl/README.md).
# Usage: xdcl/scripts/check.sh [--no-run]
set -euo pipefail

workspace="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=toolkit.sh
. "$workspace/scripts/toolkit.sh"

cd "$workspace"

echo "==> Sync"
if ! sync_output="$(xdcl-gradle-sync sync . 2>&1)"; then
  echo "$sync_output"
  exit 1
fi

echo "==> Validate"
files=()
while IFS= read -r file; do files+=("$file"); done < <(
  find . \( -name build -o -name .gradle -o -name .kotlin \) -prune -o \
    \( -name '*.xdsl' -o -name '*.xdcl' \) -type f -print | sort
)
# Settings scripts only validate against a synced manifest (in the zero-config fallback they fail
# with "'settings' is not a valid template"), so a clean run also proves the sync was picked up.
printf '    %s\n' "${files[@]}"
if ! diagnostics="$(xdcl-lsp-client validate --root . "${files[@]}")"; then
  echo "$diagnostics"
  exit 1
fi
echo "    ${#files[@]} files clean"

if [ "${1:-}" != "--no-run" ]; then
  echo "==> Run hello-world"
  output="$(./gradlew --console=plain -q :hello-world:app:run)"
  if [ "$output" != "Hello, World!" ]; then
    echo "unexpected output: $output" >&2
    exit 1
  fi
  echo "    $output"
fi

echo "OK"
