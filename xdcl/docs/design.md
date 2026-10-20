# JetBrains ecosystem on XDCL: design exploration

Milestone 1: the whole user-facing configuration of the JetBrains ecosystem as an XDCL schema, so build files can be
typed in the IDE and validate ("not red"), plus a list of problems and a rough idea of how each part could be
implemented. Implementation (reactions) is out of scope; only `jvmApplication`'s hello-world reaction exists.

Sources: the DCL implementation in this repository is the source of truth; the
[wireframes](%5BPrototype%20wireframes%5D%20JetBrains%20Ecosystem%20plugin%20for%20Declarative%20Gradle.md) give the
spirit and fill gaps (see [Deviations](#deviations-from-the-dcl-implementation-and-the-wireframes)).

## Where things are

| | |
|---|---|
| Mainline schema | [`ecosystem/src/main/xdcl/`](../ecosystem/src/main/xdcl) |
| Mainline examples | [`examples/`](../examples): `idea-wizard` and `spring-petklinik` (ports), `reference` (every property, = `dsl-reference/*.md`), `kotlinconf` (the wireframe). They use defaults and prefabs as described in [Improved examples](#improved-examples-defaults-for-policy-prefabs-for-kinds-of-project). |
| Mock third-party plugins | [`mocks/`](../mocks): Android, Ktor (with Jib), AboutLibraries, extending the JetBrains schema |
| Forks | [`forks/`](../forks), one directory per open question, each with a README |

Everything is validated by `scripts/check.sh`. To try a variant in the IDE, open `xdcl/` and edit any build file.

## Mainline: how DCL maps to XDCL

| DCL | XDCL mainline | Why |
|---|---|---|
| Project type (`jvmApplication { }`) | `template` with that rootname | 1:1. One template per project file is enforced by XDCL too. |
| Project feature that applies plugins (`serialization`, `spring`, `distribution`, `resource`, `maven`, `iosFramework`, `compose`, `parcelize`, `webpack`) | `extension`, enabled by writing its block | Matches "writing the block enables the feature". Extensions are what other plugins can contribute, and an extension's reaction fires only when enabled. |
| Configuration section (`dependencies`, `testing`, `packaging`, `publishing`, `toolchain`, `xxxPlatform`, `kotlin`, `java`, `compilerOptions`) | optional `record` property | Owned by the project type, always available; no plugin applied by writing it. |
| Where features plug in | traits: `KotlinFeatures`, `JvmApplicationFeatures`, `JvmApplicationDependencyFeatures`, `JvmApplicationPackagingFeatures`, `ApplicationPackaging`, `WebApplicationPackagingFeatures`, `LibraryFeatures`, `LibraryPublishingFeatures`, `ComposeHost`, `JvmToolchainOwner` | Traits are the public extension points for other plugins (the mocks use them). Anything not `closed` can also be extended by name. |
| Enums (`JvmVendor.ADOPTIUM`, `LibraryPlatform`, `WebSubplatform`, serialization formats) | `Symbol` refinements: `:adoptium`, `:jvm`, `:wasmJs`, `:json` | Validated, with completion and "did you mean". |
| `implementation("g:n:v")`, `implementation(project(":x"))`, `libs.x` | `implementation ["g:n:v", :projects.x, :libs.x]` | Lists of a `Dependency` union: a string, a symbol, or a record for extras (`{ module … exclude […] }`, `{ platform … }`, `{ testFixtures … }`, `{ npm …, version … }`). |
| `freeCompilerArgs += listOf(…)` | `freeCompilerArgs [ … ]`, or `freeCompilerArgs { append [ … ] }` on top of a default | XDCL has list ops: `seed` (plain literal), `append`, `prepend`, `remove`, `reset`, `restrict`. |
| `useJUnitPlatform()` (wireframe) | `useJUnitPlatform true` | Already a property in the DCL implementation. |
| `defaults { library { … } }` | `defaults [ library { … } ]` in `settings` | Plus heads the DCL doesn't have: trait (`JvmToolchainOwner { … }`), extension (`compose { … }`, the wireframe's "defaults for optional blocks"), and record. |
| — | `prefabs [ name: head { … } ]` | New: named, opt-in bundles of defaults (see [`forks/petklinik-idiomatic`](../forks/petklinik-idiomatic)). |
| `platforms = listOf(jvm, web)` | `platforms [:jvm, :web]`, required | Deliberately the minimal working config; platform blocks stay optional configuration. |

Smaller choices: `jvmTarget 21` (an `Int`, like `releaseVersion`) instead of `JVM_21`; language/API versions as strings
(`"2.3"`, since `2.10` as a `Float` would be `2.1`); `KotlinVersion` and `JavaVersion` are refinements, so `jvmTarget 7`
is an error. Kotlin compiler options are a curated subset of KGP's (see [P6](#p6-kotlin-compiler-options-are-mirrored-by-hand)).

## Improved examples: defaults for policy, prefabs for kinds of project

After walking through the forks, the examples were rewritten in place with one rule:

- **Settings `defaults`** hold build-wide policy and never switch a feature on: JDK version, JUnit Platform, library
  platforms, Android SDK levels, "serialization uses JSON" (`serialization { enabledFormats [:json] }`), the Compose
  resources package. Lists use `append`, so projects keep writing plain lists.
- **Prefabs** describe kinds of project. They switch features on and bring their dependencies: `springBootApp`
  (spring-petklinik), `ktorServer` and `composeDesktopApp` (kotlinconf). A project opts in by starting with the prefab's
  name instead of the project type. The prefabs are defined in each build's settings to showcase them; they are
  candidates to be shipped by the plugin that owns the feature (Spring, Ktor, the JetBrains ecosystem).

The effective definition of every rewritten project is identical to the previous version's (checked by comparing
`xdcl-lsp-client effective` output). Project files got shorter (spring-petklinik `backend` 44 to 31 lines, `ai` 26 to
15, kotlinconf `backend` 61 to 50); the settings got longer by the prefab definitions, which plugins would take over.
The previous one-to-one ports are in git history (`git show c849342:xdcl/examples/spring-petklinik/…`).

## Forks

| Fork | Question | Decision |
|---|---|---|
| [`petklinik-idiomatic`](../forks/petklinik-idiomatic) | How should a build use defaults: ambient defaults vs a named prefab? | Prefabs adopted |
| [`platform-dependencies`](../forks/platform-dependencies) | Per-platform dependencies/tests under `dependencies { jvmPlatform { } }` or under `jvmPlatform { dependencies { } }`? | Keep the mainline |
| [`feature-switches`](../forks/feature-switches) | How does a project switch off a feature a default switched on? `serialization false` vs `spring { enabled false }` | Not adopted |
| [`spring-single-block`](../forks/spring-single-block) | One `spring { }` block instead of three | Keep three blocks |
| [`kotlin-opt-in`](../forks/kotlin-opt-in) | Does `kotlin { }` enable Kotlin (Java/Kotlin symmetry)? | Keep the mainline: Kotlin and Java always on, react to source files |
| [`android-by-agp`](../forks/android-by-agp) | Does the Android plugin own `androidPlatform { }`? | No change for the prototype; owned by Android in the final design |
| [`dependency-notation`](../forks/dependency-notation) | `:projects.shared` vs `":shared"` vs `{ project ":shared" }` | Discarded for now: to discuss with Gradle |

Forks are copies of the mainline schema (`scripts/new-fork.sh`) with one change each; they don't follow later mainline
changes.

## Problems and things that can't be expressed

Each one has a "where it shows" pointer and what an implementation could do about it.

### P1. No constraints across properties

The schema can't say "`iosPlatform { }` requires `:ios` in `platforms`", "`dependencies { spring { } }` requires
`spring { }` on the application", or "a non-JVM library needs Kotlin". Such files validate and fail later.
*Implementation:* the reaction checks and reports with a good message (the DCL implementation already reports
problems as diagnostics). *Schema:* where possible, make it structural, as in [`spring-single-block`](../forks/spring-single-block).

### P2. A feature enabled by a default can't be disabled

No `null`; `reset` only works on lists and maps. Shows in petklinik, where every `jvmApplication` gets Spring.
*Options:* [`feature-switches`](../forks/feature-switches) (`Bool | block` or `enabled`), or prefabs instead of
ambient defaults ([`petklinik-idiomatic/prefab`](../forks/petklinik-idiomatic/prefab)).

### P3. A plain list in a project replaces a plain list from the settings

`defaults [ jvmApplication { dependencies { implementation ["a"] } } ]` plus a project's `implementation ["b"]` gives
`["b"]`. With `implementation { append ["a"] }` in the settings, the project's plain list keeps it: `["b", "a"]`.
*Convention:* defaults that add to lists use `append`; projects write plain lists. Worth documenting prominently,
since it's the opposite of what a Gradle user expects from `defaults`.

### P4. Version catalog and project accessors are not validated

`:libs.x` and `:projects.x` are symbols, and any symbol validates. Scalars can't reference catalog versions at all
unless the schema allows it (`compileSdk: Int | Symbol` in [`android-by-agp`](../forks/android-by-agp)). Shows in
KotlinConf. *Would need:* a per-build schema generated from `include [...]` and `libs.versions.toml`, which is beyond
what a plugin can ship. Details in [`dependency-notation`](../forks/dependency-notation).

### P5. Value sets can't be extended by other plugins

`LibraryPlatform` is a closed refinement, so if the Android plugin owns `androidPlatform { }`, `:android` still has
to be in the JetBrains set ([`android-by-agp`](../forks/android-by-agp)). The same applies to any enum another plugin
might want to extend.

### P6. Kotlin compiler options are mirrored by hand

DCL exposed KGP's `KotlinCommonCompilerOptions` etc. directly; an XDCL schema has to restate every option. The
mainline has a curated subset (`languageVersion`, `apiVersion`, `progressiveMode`, `optIn`, `allWarningsAsErrors`,
`suppressWarnings`, `extraWarnings`, `freeCompilerArgs`; JVM: `jvmTarget`, `javaParameters`, `moduleName`; JS:
`sourceMap`, `moduleName`; Native: `moduleName`), with `freeCompilerArgs` as the escape hatch. *Implementation:*
generate this part of the schema from the KGP API, which also keeps it in sync across Kotlin versions. Open
question: version the schema with KGP, or with the ecosystem plugin.

### P7. Reusing a block name breaks defaults heads

Three extensions are called `spring`, so the settings rule `spring { … }` is `ambiguous target 'spring'`. Validation
of project files is fine, since names are scoped per body. *Rule:* one name per feature within a plugin
([`spring-single-block`](../forks/spring-single-block)). `jvmPlatform` is reused too, but as record properties, which
are only addressed through their template (`library { jvmPlatform { … } }`), so it's harmless.

### P8. Trait and record defaults don't create absent optional blocks

`JvmToolchainOwner { toolchain { releaseVersion 17 } }` reaches `jvmApplication`, but `library.jvmPlatform` only if
the project writes `jvmPlatform { }`. A template rule (`library { jvmPlatform { … } }`) does create it.
*Workaround:* see [`petklinik-idiomatic/defaults`](../forks/petklinik-idiomatic/defaults). *Implementation:* the reaction
treats an absent `jvmPlatform` as an empty one anyway, but the effective definition (what the IDE shows) would not
show the toolchain.

### P9. Record defaults reach every use of the record type

A `LibraryDependencies { … }` rule reaches every value of that record type. The mainline keeps main and test
dependencies as separate record types, so a dependencies default never leaks into tests.

### P10. Settings repositories are minimal

Only `:mavenCentral`, `:gradlePluginPortal` and URL strings: no `google()`, no content filters (`mavenContent {
includeGroupAndSubgroups(…) }`). KotlinConf uses Google's repository URL without filters. This is Gradle's
bootstrap schema, not ours.

### P11. Tooling constraints

- The language server only loads plugin schemas located under its root, so the plugin, the mocks and every example
  are included builds of one umbrella build, and the IDE has to open `xdcl/` ([README](../README.md#decisions)).
- Schemas are scoped per jar, not per plugin id: several descriptors in one plugin project expose all its schemas
  as soon as one of them is applied. That's why the mocks are separate builds.
- Included builds are named after their directory, which has to be unique across the umbrella (hence `fork-<name>`).
- A plugin depending on another plugin's schema needs Maven coordinates for it: `group` in `gradle.properties`, as
  the plugin-development project type has no `group`.
- Regex refinements reject patterns at risk of catastrophic backtracking.
- `xdcl-gradle-sync` has to be re-run after changes to settings structure or to a descriptor. The IDE never runs it.

### P12. DCL helper functions

`kotlin("test")`, `npm(…)`, `platform(…)`, `testFixtures(…)`, `compose.desktop.currentOs`. They become strings
(`"org.jetbrains.kotlin:kotlin-test"`), records (`{ npm …, version … }`, `{ platform … }`, `{ testFixtures … }`), and
symbols (`:compose.desktop.currentOs`), which only works if the reaction resolves them.

### P13. No escape hatch

There is no imperative code at all, so everything a build needs must be in some schema. In DCL a gap could be filled
with a `build.gradle.kts` subproject, and that still works: an XDCL settings file with a plain `build.gradle.kts`
subproject builds fine (tested), so mixed builds are a migration path.

## Deviations from the DCL implementation and the wireframes

Followed the DCL implementation:

- `toolchain { releaseVersion }`, not the wireframe's `jvmLanguageVersion`.
- `androidPlatform { minSdk, compileSdk, compileSdkExtension, namespace, kotlin }`, not the wireframe's `defaultConfig`,
  `jvmTarget` and `java { compileOptions }`. `jvmTarget` goes in `kotlin { compilerOptions { } }`.
- `{ platform "…:bom:…" }` (DCL `platform(…)`), not the wireframe's `spring { imports { mavenBom(…) } }`.
- `dependencies { resources { resource [ … ] } }`, not the wireframe's `spring { resource(project(":frontend"), "static") }`.
- `spring { }` without the wireframe's `frameworkVersion`.
- `iosFramework { static }`, not `isStatic`.

Fixed or added:

- The DCL implementation calls the iOS test dependencies `iosPlatformDependencies`; the schema uses `iosPlatform`,
  like the DSL reference and every other platform.
- From the wireframes, not in the DCL implementation: `compose { resources { } }` (with `ComposeHost` so Android's
  `androidApplication` can carry it), `kotlin { parcelize { } }`, `packaging { webpack { outputFileName } }`,
  `iosFramework { baseName }`, `dependencies { androidPlatform { … debugImplementation } }`.
- The mocks (Android, Ktor, Jib, AboutLibraries) follow the wireframe's shapes; real schemas would be designed by
  those projects.

## Implementation notes (not in milestone 1)

- **Reactions.** One `Reaction<Template, Project>` per project type (proven by `jvmApplication`: applies KGP and
  `application`). One `Reaction<Extension, Project>` per feature, which fires only when the feature is enabled and
  finds its project type through `ReactionScope.ancestor(…)`. Platform-specific compiler options overriding the shared
  ones is the reaction's job: the facades give both.
- **Plugins on the classpath.** Reactions apply KGP, AGP, Spring Boot etc. by id, so those must be dependencies of
  the ecosystem plugin, which pins their versions. That's the same single-classloader constraint the DCL
  implementation already documents.
- **Dependencies.** The `Dependency` union arrives as a sealed facade; strings go to `DependencyHandler`,
  `:projects.x` and `:libs.x` symbols have to be resolved by the reaction against the project tree and the version
  catalog, and records map to Gradle's `platform`, `testFixtures`, `exclude`, `npm`.
- **Cross-property checks (P1)** go into reactions as problems.
- **Third-party features** (Spring, Ktor, Android) would ship their own schema plus reactions, depending on the
  JetBrains ecosystem's schema jar for the traits they extend, as the mocks do.
