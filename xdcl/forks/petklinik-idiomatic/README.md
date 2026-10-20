# Fork: spring-petklinik, written idiomatically for XDCL

Same schema as the mainline (`xdcl/ecosystem`). The question is how a real build should *use* XDCL's defaults
machinery, compared to the 1:1 port in [`examples/spring-petklinik`](../../examples/spring-petklinik).

| | Build | Shared setup lives in | A project opts in by |
|---|---|---|---|
| Port | [`examples/spring-petklinik`](../../examples/spring-petklinik) | settings `defaults` (template rules only), test dependencies repeated per project | nothing: every `jvmApplication` is a Spring app |
| A | [`defaults/`](defaults) | settings `defaults`, using `append`, trait and extension rules | nothing: every `jvmApplication` is a Spring app |
| B | [`prefab/`](prefab) | settings `prefabs`: a named `springBootApp` bundle | writing `springBootApp { … }` instead of `jvmApplication { … }` |

## What A and B use

- **`append` in defaults.** A plain list in a project *replaces* a plain list from the settings, but keeps what the
  settings `append`ed. The settings therefore write `implementation { append ["…kotlin-reflect"] }` and every
  project keeps writing a plain `implementation [...]`.
- **Trait rule** `JvmToolchainOwner { toolchain { releaseVersion 17 } }` sets the toolchain of `jvmApplication` and of
  `library.jvmPlatform` in one place. Caveat: a default never creates an absent optional block from a trait or
  record rule, so the `library` rule writes an empty `jvmPlatform { }` to make it reachable.
- **Extension rule** `serialization { enabledFormats [:json] }` configures serialization wherever a project enables
  it, but enables it nowhere. Projects write just `serialization { }`.

## A vs B

- A keeps the project files shortest, but `spring { }` reaches *every* `jvmApplication`, and there is no way to turn
  off an extension or an optional block that a default enabled (no `null`, `reset` only works on lists and maps).
  A non-Spring JVM application would need its own build, or the opt-out fork.
- B makes the intent visible in the project file (`springBootApp { … }`), keeps plain `jvmApplication { … }`
  Spring-free, and needs no opt-out. Effective definitions are identical: a prefab only shows up in provenance
  (`settings prefab 'springBootApp' for template jvmApplication`), and reactions still see a `jvmApplication`.
  The cost is a build-specific vocabulary: `springBootApp` means something only in this build.
  Prefabs can also ship with a plugin (the Spring plugin could provide `springBootApp` itself).
