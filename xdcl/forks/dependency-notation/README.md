# Fork: how to write project and catalog dependencies

**Decision:** Discarded for now: a larger topic to discuss with Gradle. The mainline notation stays as is.

**Problem.** DCL has `project(":shared")`, `projects.shared`, `libs.kotlinx.datetime` and `testFixtures(…)`. XDCL has
no function calls and no accessors, only values validated against the schema, and the schema comes from a plugin. So
it can't know the build's projects or its version catalog.

**Notations, all accepted by this fork** (example: [`dependency-notation-example/app`](dependency-notation-example/app/build.gradle.xdcl)):

| Notation | Example | Checked by the editor |
|---|---|---|
| Accessor symbol (mainline) | `:projects.shared`, `:libs.kotlinx.datetime` | that it's a symbol; any symbol passes (`:projcts.shared`) |
| String | `":shared"`, `"g:n:v"` | nothing: a path is just a coordinate starting with `:` |
| Record (fork) | `{ project ":shared" }`, `{ catalog "kotlinx.datetime" }` | path shape, by a regex refinement (`{ project "shared" }` fails) |

Schema: `ProjectDependency` and `CatalogDependency` in
[`common.xdsl`](fork-dependency-notation/src/main/xdcl/common.xdsl). The mainline keeps the symbols: they read like
Kotlin DSL accessors (the wireframes write `projects.shared` / `libs.…`), they're the shortest, and Gradle's own XDCL
docs suggest `String | Symbol` for exactly this ("which literal form was written tells a catalog reference from an
arbitrary string").

## Findings

- **No notation can be validated against the real build.** A type-safe check would need a per-build schema, e.g. a
  refinement `type ProjectAccessor = Symbol where it in [:projects.shared, …]` generated from `include [...]` and
  `libs.versions.toml`. Plugin schemas are fixed when the plugin is built, so this would need support from Gradle
  (generated build-local schemas) or from the IDE.
- **The records are noisy** for the most common dependency kind. They do earn their place for things that need more
  than a name: `{ module …, exclude [ … ] }`, `{ platform … }`, `{ testFixtures … }`, `{ npm …, version … }`
  (mainline).
- **Regexes are guarded against ReDoS**: `^(:[A-Za-z0-9._-]+)+$` is rejected by codegen as "potentially
  catastrophic-backtracking", so refinements have to be written with a single quantifier.
