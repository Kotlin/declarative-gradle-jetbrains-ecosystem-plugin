# JetBrains ecosystem plugin on XDCL (exploration)

Exploration of the JetBrains ecosystem plugin on [XDCL](https://gradle.github.io/xdcl-starter-kit/), the next
generation of Declarative Gradle: plugins ship a `.xdsl` schema, build scripts are `.gradle.xdcl` data files
validated against it.

This directory is a separate Gradle build from the DCL prototype in the repository root, because XDCL needs its
own Gradle distribution. The DCL prototype stays the reference for the design.

## Layout

```
xdcl/
├── settings.gradle.xdcl      umbrella build: includes the plugin and every example build (see Decisions)
├── settings.gradle           comment-only marker, so IntelliJ IDEA recognizes the Gradle build
├── gradlew, gradle/          wrapper pinned to the XDCL-enabled Gradle distribution
├── ecosystem/                the `org.jetbrains.ecosystem` plugin build
│   ├── settings.gradle.xdcl, build.gradle.xdcl
│   └── src/main/
│       ├── xdcl/jetbrains-ecosystem.xdsl         the schema: what users can write
│       ├── xdcl/org.jetbrains.ecosystem.xdcl     the plugin descriptor (file name = plugin id)
│       └── kotlin/…                              reactions: evaluated config -> Gradle API
├── examples/
│   └── hello-world/          consumer build: `jvmApplication { mainClass "…" }`
└── scripts/check.sh          sync + validate every XDCL file + run hello-world
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
