# Fork: `kotlin { }` enables Kotlin

**Decision:** Not adopted: Kotlin and Java are always enabled; the implementation reacts to the presence of source files. `kotlin { }` holds settings only.

**Question.** The wireframes say `kotlin { } // Enabling Kotlin support and Kotlin specific configuration`, and argue
for "full symmetry between Java and Kotlin": a Java project uses the same project type, and adding Kotlin should be
low-commitment. The DCL implementation always applies Kotlin, and `kotlin { }` is just configuration.

**Fork.** For `jvmApplication` and `library`, `kotlin { }` is an extension: without it the project is Java only, and
writing it is the whole migration. `webApplication` stays Kotlin-only.

```
jvmApplication {                      library {
  mainClass "xyz.yahor.app.App"         platforms [:jvm]
  java { … }                            kotlin {                 // the whole "add Kotlin" change
}                                         serialization { }
                                        }
                                      }
```

Schema: `JvmApplicationKotlin` in [`jvm-application.xdsl`](fork-kotlin-opt-in/src/main/xdcl/jvm-application.xdsl),
`LibraryKotlin` in [`library.xdsl`](fork-kotlin-opt-in/src/main/xdcl/library.xdsl).
Example: [`kotlin-opt-in-example/`](kotlin-opt-in-example).

## Trade-offs

- **For the fork.** The Java/Kotlin symmetry from the wireframes is visible in the build file. A settings default
  `library { kotlin { } }` makes a whole build Kotlin.
- **Against.** Kotlin-only users write `kotlin { }` everywhere (or once, in a default). A `library` with a non-JVM
  platform needs Kotlin anyway, so `kotlin { }` would be implied there, a rule the schema can't express.
- **XDCL-specific.** The extension carries the `KotlinFeatures` trait (`extension … with KotlinFeatures for …`), so
  `serialization { }` still nests inside `kotlin { }`, and enabling serialization structurally requires Kotlin.
