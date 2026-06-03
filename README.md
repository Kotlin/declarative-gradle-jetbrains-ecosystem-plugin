[![Incubator](https://jb.gg/badges/incubator-plastic.svg)](https://github.com/JetBrains#jetbrains-on-github)

# JetBrains ecosystem plugin

The JetBrains ecosystem plugin is an experimental plugin for [Declarative Gradle](https://declarative.gradle.org/), Gradle's new declarative build language. You can use this plugin to explore Declarative Gradle itself and a new approach to organizing Kotlin build scripts.

You can mix Declarative Gradle subprojects with non-declarative ones. In its current state, the JetBrains ecosystem plugin 
lets you create [jvmApplication](./dsl-reference/jvm-application.md), [webApplication](./dsl-reference/web-application.md),
and [library](./dsl-reference/library.md) (supporting `jvm`, `web`, and `ios` platforms) subprojects.

While developing this plugin, we've significantly changed the mental model behind how Kotlin application build scripts can be built. We would appreciate your feedback on the design choices we've made.

* Share your feedback in the `#declarative-gradle` channel in [Slack](https://slack-chats.kotlinlang.org/c/declarative-gradle).
* Report any issues you find through [GitHub Issues](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/issues/new/choose).

> [!WARNING]
> Both Declarative Gradle and this plugin are pre-stable. Expect rough edges, breaking changes between releases, and no compatibility guarantees.

## Setup

### Switch to Gradle version `9.6.0`

> [!IMPORTANT]
> Declarative Gradle currently needs Gradle version `9.6.0-rc-1`.

You can change the Gradle version in the Gradle Wrapper from the command line or by updating the `distributionUrl` property.

For the command line, use the following:

```
./gradlew wrapper --gradle-version 9.6.0-rc-1
```

For the property, in the `gradle/wrapper/gradle-wrapper.properties` file, update the `distributionUrl` property as follows:

```
distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.0-rc-1-bin.zip
```

### Update your settings file

1. Rename your settings file from `settings.gradle(.kts)` to `settings.gradle.dcl`.
2. In your `settings.gradle.dcl` file, add this repository in the `pluginManagement {}` block:

```
pluginManagement {
    repositories {
        maven {
            url = uri("https://raw.githubusercontent.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/refs/heads/maven2")
        }
    }
}
```

3. Apply the JetBrains ecosystem plugin:
```
plugins {
    id("org.jetbrains.ecosystem").version("latest.release")
}
```

> [!TIP]
> We recommend using the `latest.release` variable to automatically stay up to date with the latest version because the project is under active development. If you prefer to use [a specific release](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/releases), set the version explicitly:

```
plugins {
    id("org.jetbrains.ecosystem").version("0.103.0")
}
```

After making these changes, your `settings.gradle.dcl` file should look like this:

```
pluginManagement {
    repositories {
        maven {
            url = uri("https://raw.githubusercontent.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/refs/heads/maven2")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
    id("org.jetbrains.ecosystem").version("latest.release")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":app")
include(":utils")

rootProject.name = "MySimpleApp"
```

### Sync and update your project

Sync your project to apply the changes and verify that everything works correctly.

> [!WARNING] 
> The build may fail with a version resolution conflict for some plugins. Declarative Gradle uses a single classloader for all projects, so you can't apply the same plugin with different versions in different projects.

To avoid conflicts, remove any version references from Kotlin plugins:

a. In `build.gradle(.kts)` files:

❌ **Don't do this:**
`kotlin("jvm").version("2.3.20")`
`id("org.jetbrains.kotlin.multiplatform").version("2.3.20")`

✅ **Do this:**
`kotlin("jvm")`
`id("org.jetbrains.kotlin.multiplatform")`

b. In the `libs.versions.toml` file:

❌ **Don't do this:**
`kotlinPluginSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "2.3.30" }`

✅ **Do this:**
`kotlinPluginSerialization = { id = "org.jetbrains.kotlin.plugin.serialization"}`

#### Known problems

1. The `include()` function with multiple arguments isn't supported in the `settings.gradle.dcl` file. You have to use a separate entry for each argument:

❌ **Don't do this:**
`include("frontend", "backend")`

✅ **Do this:**
`include("frontend")`
`include("backend")`

2. Some APIs, including all annotations, aren't available yet in Declarative Gradle.

### Enable IDE support in IntelliJ IDEA and Android Studio

1. Enable internal mode in the IDE: 
   1. Select **Help** | **Edit Custom Properties**. 
   This action opens the `idea.properties` file. If the file doesn't exist, the IDE will prompt you to create one. 
   2. Add a line with `idea.is.internal=true` and save the file.
2. Restart the IDE.
3. Select **Tools** | **Internal Actions** | **Registry**.
4. Search for Declarative Gradle keys by typing "declarative".
5. Enable the `gradle.declarative.studio.support` and `gradle.declarative.ide.support` keys.
6. Restart the IDE again.

### Enable Declarative DSL auto-completion in Kotlin DSL

1. Check that your project has a `gradle.properties` file. If it doesn't exist, create one in the root of your project.
2. In your `gradle.properties` file, add the following property:

```
org.gradle.kotlin.dsl.dcl=true
```

3. Sync the project.

This property enables auto-completion for the Declarative DSL in `.kts` files, supporting a smooth migration.

## Try the prototype

> [!IMPORTANT] 
> Keep in mind that this is a prototype, so not every project can be migrated yet.

Because Declarative Gradle subprojects can coexist with non-declarative ones, you can start by selecting a single subproject and converting it first.

Start with these resources:

- The [`examples`](examples), which show simple applications migrated to the Declarative DSL.
- The [`dsl-reference`](dsl-reference), which documents the DSL's current capabilities. Expect differences from the regular Kotlin DSL. The mapping is intentionally not 1:1, and some features aren't available yet.

### Known Declarative Gradle limitations

1. Version catalogs aren't supported yet. When declaring a dependency, you must provide the dependency coordinates instead of a version catalog reference.
2. The `kotlin()` alias for dependencies isn't supported. You must provide the full dependency coordinates.
3. Combining the JetBrains ecosystem plugin with the Android ecosystem plugin may not work in all cases. An explicit `android` type in the `library` project type is not supported yet, but the `jvm` platform should support most of the general use cases.

## Examples of migrated applications

* [A multi-module Kotlin project from IntelliJ IDEA's new project wizard](examples/idea-wizard/)
* [Spring-PetKlinik – A Kotlin full-stack Spring sample application](examples/spring-petklinik/)

## DSL reference

See the following for useful entries in the DSL reference:

* [jvmApplication](dsl-reference/jvm-application.md)
* [webApplication](dsl-reference/web-application.md)
* [library](dsl-reference/library.md)

## Feedback and issue reporting

While developing this plugin, we've significantly changed the mental model behind how Kotlin application build scripts can be built. We would appreciate your feedback on the design choices we've made.

* Share your feedback in the `#declarative-gradle` channel in [Slack](https://slack-chats.kotlinlang.org/c/declarative-gradle).
* Report any issues you find through [GitHub Issues](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/issues/new/choose).
