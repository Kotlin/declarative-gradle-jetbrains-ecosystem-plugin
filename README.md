[![Incubator](https://jb.gg/badges/incubator-plastic.svg)](https://github.com/JetBrains#jetbrains-on-github)

# JetBrains ecosystem plugin

The JetBrains ecosystem plugin is an experimental plugin for [Declarative Gradle](https://declarative.gradle.org/), Gradle's new declarative build language. You can use this plugin to explore Declarative Gradle itself and a new approach to organizing Kotlin build scripts.

You can mix Declarative Gradle subprojects with non-declarative ones. In its current state, the JetBrains ecosystem plugin 
lets you create [jvmApplication](./dsl-reference/jvm-application.md), [webApplication](./dsl-reference/web-application.md),
and [library](./dsl-reference/library.md) (supporting `jvm`, `web`, and `ios` platforms) subprojects.

> [!WARNING]
> Both Declarative Gradle and this plugin are pre-stable. Expect rough edges, breaking changes between releases, and no compatibility guarantees.

## Setup

### Switch to Gradle version `9.6.0`

> [!IMPORTANT]
> Declarative Gradle currently needs Gradle version `9.6.0-milestone-2`.

You can change the Gradle version in the Gradle Wrapper from the command line or by updating the `distributionUrl` property.

For the command line, use the following:

```
./gradlew wrapper --gradle-version 9.6.0-milestone-2
```

For the property, in the `gradle/wrapper/gradle-wrapper.properties` file, update the `distributionUrl` property as follows:

```
distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.0-milestone-2-bin.zip
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
        mavenCentral()
        gradlePluginPortal()
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

## Convert a subproject to Declarative Gradle 

🚧🚧🚧 **Work in progress** 🚧🚧🚧

## Examples

🚧🚧🚧 **Work in progress** 🚧🚧🚧

## DSL reference

🚧🚧🚧 **Work in progress** 🚧🚧🚧

## Feedback and issue reporting

While developing this plugin, we've significantly changed the mental model behind how Kotlin application build scripts can be built. We would appreciate your feedback on the design choices we've made.

* Share your feedback in the `#declarative-gradle` channel in [Slack](https://slack-chats.kotlinlang.org/c/declarative-gradle).
* Report any issues you find through [GitHub Issues](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/issues/new/choose).