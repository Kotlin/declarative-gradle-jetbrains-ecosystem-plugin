[![Incubator](https://jb.gg/badges/incubator-plastic.svg)](https://github.com/JetBrains#jetbrains-on-github)

# JetBrains Ecosystem Plugin

A prototype JetBrains Ecosystem Plugin for [Declarative Gradle](https://declarative.gradle.org/) — Gradle's experimental declarative build language. You can use this plugin to explore both Declarative Gradle itself and the new approach to organizing Kotlin build scripts.

You can mix Declarative Gradle sub-projects with non-declarative ones. In its' current state the JetBrains Ecosystem Plugin provides an option to create `jvmApplication`, `webApplication`, and `library` (supporting `jvm` and `web` platforms) sub-projects.

⚠️ Both Declarative Gradle itself and this plugin are still in pre-stable stable. Expect rough edges, no compatibility guarantees, and breaking changes between releases.

## Setup

### Switch to Gradle version `9.6.0`

_Note: As of now a pre-release Gradle version `9.6.0-milestone-1` is needed._

The following command upgrade the Gradle Wrapper to the required version:
```
./gradlew wrapper --gradle-version 9.6.0-milestone-1
```

Another way to upgrade the Gradle version is by manually changing the `distributionUrl` property in the Wrapper’s `gradle-wrapper.properties` file in `gradle/wrapper/`. After the change it should look like this:
```
distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.0-milestone-1-bin.zip
```

### Update the Settings file

1. Rename your settings file (`settings.gradle(.kts)`) to `settings.gradle.dcl`.
2. Add this repository in `pluginManagement`:
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
3. Apply the JetBrains Ecosystem Plugin
```
plugins {
    id("org.jetbrains.ecosystem").version("latest.release")
}
```
We advise to use `latest.release` as the project is under active development with new updates being released daily. If you prefer to pin a version to [a specific release](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/releases) simplify specify the version explicitely:
```
plugins {
    id("org.jetbrains.ecosystem").version("0.74")
}
```
3. After the changes your Settings file should look like this:
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
4. Sync project to make sure the changes were applied correctly. 
⚠️ The build may fail with a version resolution conflict for some of the plugins. The Declarative Gradle uses a single classloader for all projects which means you cannot apply the same plugin with different version in different projects.
5. Remove version references of any Kotlin plugins:
a. In `build.gradle(.kts)` files:
❌ **Don't do this:**
`kotlin("jvm").version("2.3.20")`
`id("org.jetbrains.kotlin.multiplatform").version("2.3.20")`
✅ **Do this:**
`kotlin("jvm")`
`id("org.jetbrains.kotlin.multiplatform")`
b. In `libs.versions.toml`:
❌ **Don't do this:**
`kotlinPluginSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "2.3.30" }`
✅ **Do this:**
`kotlinPluginSerialization = { id = "org.jetbrains.kotlin.plugin.serialization"}`

### Enable IDE support in IntelliJ IDEA and Android Studio
1. Enable the IDE internal mode by selecting _Help_ -> _Edit Custom Properties_. This selection opens the `idea.properties` file. If it does not exist, the IDE will prompt to create one. Add a line with `idea.is.internal=true` and save the file.
2. Restart the IDE
3. Open _Tools_ -> _Internal Actions_ -> _Registry_
4. Search for the Declarative Gradle flags by typing declarative
5. Enable the `gradle.declarative.studio.support` and `gradle.declarative.ide.support` flags
6. Restart the IDE

## How to convert a sub-project to the Declarative Gradle? 

🚧🚧🚧 **Work in progress** 🚧🚧🚧

## Examples

🚧🚧🚧 **Work in progress** 🚧🚧🚧

## DSL Reference

🚧🚧🚧 **Work in progress** 🚧🚧🚧

## Feedback and issues

We've changed quite a bit in a mental model behind how Kotlin application build scripts can be built. We would apreciate your feedback on design choices we've made.
* Please provide feedback on `#declarative-gradle` channel on [KotlinLang Slack](https://slack-chats.kotlinlang.org/?_cl=MTsxOzE7OEZiZXJieTM5dTg3S09oVUpoM21ZZTB3RkRvanBRQVl2elhrMnJ2N1hSc01hTkUwOWRvY3JtVDczeGJPWlViTjs=&_cl=MTsxOzE7OEZiZXJieTM5dTg3S09oVUpoM21ZZTB3RkRvanBRQVl2elhrMnJ2N1hSc01hTkUwOWRvY3JtVDczeGJPWlViTjs=&_gl=1*1wo5sn9*_gcl_au*MTA5MzcwNjY2Mi4xNzcwODA3NjExLjE2NTI5NjM2NTguMTc3NDI1NzU0OC4xNzc0MjU3NTQ4*_ga*NzczNDU5MDM2LjE3NDY2OTg2Njk.*_ga_9J976DJZ68*czE3NzgyNTIxODckbzE1MSRnMSR0MTc3ODI1MjUxOCRqMzUkbDAkaDA.)
* Please report any issues found through [GitHub Issues](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin/issues/new/choose)