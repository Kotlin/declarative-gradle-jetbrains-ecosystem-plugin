# JetBrains ecosystem plugin on XDCL (exploration)

Exploration of the JetBrains ecosystem plugin on [XDCL](https://gradle.github.io/xdcl-starter-kit/), the next
generation of Declarative Gradle: plugins ship a `.xdsl` schema, build scripts are `.gradle.xdcl` data files
validated against it.

This directory is a separate Gradle build from the DCL prototype in the repository root, because XDCL needs its
own Gradle distribution. The DCL prototype stays the reference for the design.

## Layout

```
xdcl/
├── settings.gradle.xdcl      umbrella build: includes every build below (see Decisions)
├── settings.gradle           comment-only marker, so IntelliJ IDEA recognizes the Gradle build
├── gradlew, gradle/          wrapper pinned to the XDCL-enabled Gradle distribution
├── docs/
│   ├── design.md             design exploration: decisions, forks, problems  <- start here
│   └── [Prototype wireframes] …md
├── ecosystem/                the `org.jetbrains.ecosystem` plugin build (the mainline)
│   └── src/main/
│       ├── xdcl/*.xdsl                           the schema: what users can write
│       ├── xdcl/org.jetbrains.ecosystem.xdcl     the plugin descriptor (file name = plugin id)
│       └── kotlin/…                              reactions: evaluated config -> Gradle API (hello world only)
├── examples/
│   ├── hello-world/          `jvmApplication { mainClass "…" }`, runs
│   ├── idea-wizard/          port of examples/idea-wizard
│   ├── spring-petklinik/     port of examples/spring-petklinik, with a `springBootApp` prefab
│   ├── reference/            every property of every project type (= dsl-reference/*.md)
│   └── kotlinconf/           the KotlinConf app from the wireframes, with `ktorServer` and `composeDesktopApp` prefabs
├── mocks/                    schema-only Android, Ktor and AboutLibraries plugins extending the ecosystem
├── forks/                    alternative designs, one directory per question, each with a README
└── scripts/
    ├── check.sh              sync + validate every XDCL file + run hello-world
    ├── sync.sh               refresh the schema manifest for the IDE
    └── new-fork.sh           start a schema fork from the mainline
```

## Setup

Versions used (all built from one XDCL commit, see the [Downloads](https://gradle.github.io/xdcl-starter-kit/downloads.html) page):

| Package | Version |
|---|---|
| Toolkit (`xdcl-lsp`, `xdcl-lsp-client`, `xdcl-fmt`, `xdcl-gradle-sync`) | `0.1.0-dev.20261008.gd14cf948` |
| IntelliJ plugin | `0.1.0-dev.20261008.gd14cf948` |
| Gradle distribution | `9.8.0-branch-xdclRelease-20261012070500+0000` (pinned in the wrapper) |

### 1. Toolkit

```shell
V=0.1.0-dev.20261008.gd14cf948
curl -LO https://gradle.github.io/xdcl-starter-kit/toolkit/xdcl-toolkit-$V.zip
curl -LO https://gradle.github.io/xdcl-starter-kit/toolkit/xdcl-toolkit-$V.zip.sha256
sha256sum -c xdcl-toolkit-$V.zip.sha256
unzip xdcl-toolkit-$V.zip -d ~/xdcl
```

The scripts in `scripts/` find the toolkit in `~/xdcl` (override with `XDCL_TOOLKIT`), so it does not need to be on
`PATH`. Put its `bin/` on `PATH` only if you want to call the tools directly. The IntelliJ plugin bundles its own
language server; the toolkit is needed for `xdcl-gradle-sync`, which IntelliJ never runs.

The toolkit's `skills/` folder contains Claude Code skills; they are used as reference documentation only, not
installed.

### 2. IntelliJ IDEA

1. Download [`xdcl-intellij-0.1.0-dev.20261008.gd14cf948.zip`](https://gradle.github.io/xdcl-starter-kit/toolkit/xdcl-intellij-0.1.0-dev.20261008.gd14cf948.zip).
2. **Settings | Plugins | ⚙ | Install Plugin from Disk…**, pick the zip, accept installing **LSP4IJ**, restart.
3. Run the sync once (below), then open the `xdcl/` directory (not the repository root) as a project. The language
   server only sees the schemas and the sync manifest of the directory opened as the project.
4. Optional: **Tools | XDCL | Show Effective Definition** and **Explain Value at Cursor** show resolved values and
   where they come from. Inlay hints need **Settings | Editor | Inlay Hints** enabled.

### 3. Sync, validate, run

```shell
cd xdcl
scripts/sync.sh                  # re-run after changing settings (plugins/include) or a plugin descriptor
scripts/check.sh                 # sync + validate all .xdsl/.xdcl files + run hello-world
./gradlew :hello-world:app:run   # prints "Hello, World!"
```

Editing an `.xdsl` schema needs no re-sync; the language server reads schemas from source live.

## Setup and workflow for agents

Instructions for coding agents working in `xdcl/`. All commands run from `xdcl/`.

### Setup

1. **JDK 17 or newer** must be available (`java -version`). Everything in the toolchain runs on it.
2. **Toolkit.** The expected version is `XDCL_TOOLKIT_VERSION` in [`scripts/toolkit.sh`](scripts/toolkit.sh), the single
   source of truth. Check `~/xdcl/xdcl-toolkit-<version>/bin/xdcl-gradle-sync` exists. If not, install it with the
   commands in [Toolkit](#1-toolkit) above, verifying the checksum, into `~/xdcl`. If it lives elsewhere, set
   `XDCL_TOOLKIT` to its directory.
3. **Gradle** comes from the wrapper (pinned to the XDCL-enabled distribution). Don't use a `gradle` from `PATH`, and
   don't change the wrapper version: the toolkit, the IntelliJ plugin and the distribution have to come from the same
   XDCL commit (see the [Downloads](https://gradle.github.io/xdcl-starter-kit/downloads.html) page).
4. **Verify:** `scripts/check.sh` must end with `OK`. The first run downloads the Gradle distribution (about 150 MB).

To call the CLIs directly, put the toolkit on `PATH` for the shell session:
`export PATH="$HOME/xdcl/xdcl-toolkit-<version>/bin:$PATH"`.

### Workflow

- **Validate after every edit** of an `.xdsl` or `.xdcl` file: `scripts/check.sh --no-run` (sync + validate every
  file). This is the check; a Gradle build is not needed to validate XDCL files.
- **Validate single files** without syncing: `xdcl-lsp-client validate --root . <files…>`. Always pass `--root .` (the
  `xdcl/` directory), never an example's own directory: the schemas are only visible from the umbrella build.
  Exit codes: 0 clean, 1 diagnostics, 2 nothing validated (read stderr; `cannot read file` means a wrong path).
- **Re-sync** (`scripts/sync.sh`, or `check.sh`, which syncs) after changing `pluginManagement`, `plugins` or `include`
  in any settings file, or a plugin descriptor (`<plugin-id>.xdcl`). Schema (`.xdsl`) edits need no sync.
- **Inspect resolved values** with `xdcl-lsp-client effective --root . <build file>` (values and where each comes from)
  and `xdcl-lsp-client explain --root . <build file>` (every contribution, including overridden ones).
- **When refactoring examples** (moving things into defaults or prefabs), compare `effective` output before and after:
  the resolved values must stay the same.
- **New builds** (examples, mocks, forks) must be added to `includedBuilds` in the umbrella
  [`settings.gradle.xdcl`](settings.gradle.xdcl), and their directory names must be unique across the umbrella (Gradle
  names included builds after their directory). A plugin build depending on the ecosystem schema needs the ecosystem
  build's `group` (see [`ecosystem/gradle.properties`](ecosystem/gradle.properties)) and its own separate build: schemas
  are scoped per jar, not per plugin id.
- **New schema forks** start with `scripts/new-fork.sh <name>`; record the question, the trade-offs and the decision in
  `forks/<name>/README.md` and in the forks table of [`docs/design.md`](docs/design.md).
- **Reference documentation** for both languages and the Gradle integration is in the toolkit's `skills/` folder
  (`xdcl-lang/skills/xdcl-authoring/reference/` and `xdcl-gradle-lang/skills/*/reference/`). Read it there; don't install
  the skills as plugins.
- **Design context** is in [`docs/design.md`](docs/design.md) (decisions, forks, known problems) and the
  [wireframes](docs/%5BPrototype%20wireframes%5D%20JetBrains%20Ecosystem%20plugin%20for%20Declarative%20Gradle.md). The DCL
  implementation in the repository root is the source of truth for the DSL surface.

## Decisions

### One umbrella build for the plugin and all examples

The language server only loads plugin schemas located under its root (the directory with the sync manifest, i.e.
the project opened in the IDE). An example build that includes the plugin from outside its own directory
(`includedBuilds ["../../ecosystem"]`) builds and runs fine, but in the editor every project script is red with
`'jvmApplication' is not a valid template`. Things that did not help:

- Validating with a common parent directory as root: without a manifest the server falls back to a plain scan of
  every `.xdsl`. Project scripts pass, but every `settings.gradle.xdcl` fails with `'settings' is not a valid template`.
- A `build-logic` symlink inside the example to the plugin: it isn't followed.

So `xdcl/settings.gradle.xdcl` includes the plugin and every example build through `pluginManagement.includedBuilds`.
One sync at `xdcl/` produces a manifest covering all builds, and the IDE project is `xdcl/`. Each example still
has its own `settings.gradle.xdcl`, so per-example settings (defaults, plugins) stay realistic. The examples share
the umbrella's wrapper; run them through the umbrella (`./gradlew :<example>:<project>:<task>`).

### Kotlin reactions applying KGP

Reactions are written in Kotlin (`xdclGradlePluginKotlin`), and the Kotlin Gradle plugin is an `implementation`
dependency of the plugin, so reactions can apply `org.jetbrains.kotlin.jvm` and friends to consuming projects. The
hello-world reaction proves the loop: schema -> config -> reaction -> KGP + `application` -> `run`.
