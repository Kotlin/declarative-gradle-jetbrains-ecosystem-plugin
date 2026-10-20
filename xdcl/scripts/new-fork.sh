#!/usr/bin/env bash
# Creates a schema fork: a copy of the mainline schema (ecosystem/src/main/xdcl/*.xdsl) as its own schema-only plugin.
#
#   scripts/new-fork.sh <name>
#
# creates forks/<name>/fork-<name>, with plugin id `org.jetbrains.ecosystem.forks.<name without dashes>` and the schema
# packages renamed to match, so a fork never clashes with the mainline or another fork. Then edit the fork's schema,
# add consumer builds under forks/<name>/ (e.g. <name>-example; Gradle names included builds after their directory, so
# directory names must be unique across the umbrella), and add them to the umbrella settings.gradle.xdcl.
set -euo pipefail

name="${1:?usage: new-fork.sh <name>}"
package_name="$(echo "$name" | tr -d '-')"
workspace="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
plugin="$workspace/forks/$name/fork-$name"

if [ -e "$plugin" ]; then
  echo "error: $plugin already exists" >&2
  exit 1
fi
mkdir -p "$plugin/src/main/xdcl"

cat > "$plugin/settings.gradle.xdcl" <<EOF
settings {
  rootProject {
    name "fork-$name"
  }
  plugins [
    { id "plugin-development-ecosystem" }
  ]
}
EOF

cat > "$plugin/build.gradle.xdcl" <<EOF
// Schema-only fork of the JetBrains ecosystem plugin, see ../README.md.
xdclGradlePlugin {
}
EOF

cat > "$plugin/src/main/xdcl/org.jetbrains.ecosystem.forks.$package_name.xdcl" <<EOF
plugin {
  displayName "JetBrains ecosystem, fork $name"
  description "Schema-only fork of the JetBrains ecosystem plugin, see forks/$name/README.md."
}
EOF

for schema in "$workspace"/ecosystem/src/main/xdcl/*.xdsl; do
  sed -E "s/^(package|import) org\.jetbrains\.ecosystem/\1 org.jetbrains.ecosystem.forks.$package_name/" \
    "$schema" > "$plugin/src/main/xdcl/$(basename "$schema")"
done

echo "created $plugin (plugin id org.jetbrains.ecosystem.forks.$package_name)"
