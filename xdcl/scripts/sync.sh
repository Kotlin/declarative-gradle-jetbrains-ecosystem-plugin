#!/usr/bin/env bash
# Re-writes the schema manifest the language server (and so IntelliJ) reads. Run it after changing
# pluginManagement / plugins / include in any settings.gradle.xdcl, or a plugin descriptor.
# Editing an .xdsl schema needs no sync.
set -euo pipefail

workspace="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=toolkit.sh
. "$workspace/scripts/toolkit.sh"

cd "$workspace"
xdcl-gradle-sync sync .
