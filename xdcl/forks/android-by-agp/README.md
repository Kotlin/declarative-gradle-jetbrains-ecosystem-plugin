# Fork: the Android plugin owns `androidPlatform { }`

**Decision:** No change for the prototype: `androidPlatform { }` stays in the JetBrains schema. In the final design it is owned by the Android plugin.

**Question.** The wireframes mark `androidPlatform { }` as "Software feature provided by Android External Target
plugin". The DCL implementation declares it in the JetBrains schema and only requires the Android ecosystem plugin
to be applied. XDCL lets another plugin extend our types, so the Android plugin can own the block.

**Fork.**

- The JetBrains schema ([`fork-android-by-agp`](fork-android-by-agp/src/main/xdcl/library.xdsl)) has no Android blocks.
- The Android mock ([`fork-android-by-agp-android`](fork-android-by-agp-android/src/main/xdcl/android.xdsl)) contributes
  `androidPlatform { }` to every `library` (through the trait `LibraryFeatures`), and `androidPlatform { }` to the
  library's `dependencies` (by the record's name, without any trait the JetBrains schema planned for it). It reuses
  JetBrains types (`KotlinJvmPlatformCompilation`, `Dependency`), and its `androidApplication` carries `ComposeHost`.
- API levels are `Int | Symbol`, so the wireframe's `compileSdk = libs.versions.android.compileSdk` can be written:
  `compileSdk :libs.versions.android.compileSdk`.

Example: [`android-by-agp-example/`](android-by-agp-example). User-facing, nothing changes: the files read exactly like
the mainline KotlinConf example. The difference is who owns, documents and evolves the block.

## Findings

- **Works:** a separate plugin build depends on the JetBrains schema (`api ["org.jetbrains.ecosystem.forks:…"]`,
  resolved from the included build thanks to `group` in `gradle.properties`), imports its package, and extends
  its traits and records. Without the Android plugin in `settings.plugins`, `androidPlatform { }` is an unknown
  property.
- **Problem:** `:android` must stay in the JetBrains `LibraryPlatform` set: a refinement (`Symbol where it in […]`) is
  closed, and another plugin can't add a value to it. A truly open platform list would be `[Symbol]`, which loses
  validation and completion of the values.
- **Problem:** an `Int | Symbol` union makes any symbol valid (`compileSdk :typo` validates), since there is no
  version catalog in the schema world.
