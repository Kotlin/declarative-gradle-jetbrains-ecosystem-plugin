# Fork: platform-specific dependencies and tests inside the platform block

**Question.** Where does configuration of one platform live: grouped by *concern* (`dependencies`, `testing`) with
platforms inside, or grouped by *platform* with concerns inside?

```
// Mainline (= current DCL)                      // Fork
library {                                        library {
  jvmPlatform { toolchain { … } }                  jvmPlatform {
  dependencies {                                     toolchain { … }
    implementation [ … ]                             dependencies { implementation [ … ] }
    jvmPlatform { implementation [ … ] }             testing {
  }                                                    useJUnitPlatform true
  testing {                                            dependencies { implementation [ … ] }
    jvmPlatform { useJUnitPlatform true }            }
    dependencies {                                 }
      implementation [ … ]                         dependencies { implementation [ … ] }
      jvmPlatform { implementation [ … ] }         testing { dependencies { implementation [ … ] } }
    }                                            }
  }
}
```

Schema: [`fork-platform-dependencies/src/main/xdcl/library.xdsl`](fork-platform-dependencies/src/main/xdcl/library.xdsl).
Examples: [`platform-dependencies-example/`](platform-dependencies-example) (KotlinConf `shared`, petklinik `shared`).

## Trade-offs

- **For the fork.** "Easy migration from single platform to multiplatform": adding a platform is adding one block,
  and removing one is deleting one block. `jvmPlatform` appears once per file instead of up to three times. Platform
  dependencies sit next to the platform's toolchain and compiler options.
- **For the mainline.** The common and the platform-specific dependencies of one concern are next to each other,
  which matters when moving a dependency from `jvmPlatform` to common, the typical KMP refactoring. The top-level
  `dependencies` block looks the same in every project type.
- **XDCL-specific.** Either shape validates the same; nothing in XDCL favors one. Defaults read the same too:
  `library { jvmPlatform { dependencies { implementation { append [ … ] } } } }`.
- **Unchanged problem.** Neither shape can check that a platform block belongs to a platform in `platforms` (writing
  `iosPlatform { }` without `:ios`): XDCL has no constraints across properties. The reaction would report it.
