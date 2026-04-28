[![Incubator](https://jb.gg/badges/incubator-plastic.svg)](https://github.com/JetBrains#jetbrains-on-github)

# Declarative Gradle JetBrains ecosystem plugin

A [Declarative Gradle](https://declarative.gradle.org/) prototype plugin covering JVM ecosystem including Kotlin Multiplatform (KMP).

Currently, the plugin is tied to the specific Gradle release – 9.6.0.
 
## Available project types

- `jvmApplication` - JVM based application project

## How to try it

If you want to use this prototype plugin in your project, you should do the following steps:
- Run `gradle publishAllPublicationsToRepoLocalRepository` in this repository
- Published publications are located in the `build/repo` directory
- Ensure your project is using Gradle `9.6.0` (project is using `9.6.0-milestone-1`) release
- Change your project settings `.kts` extension to `.dcl` and ensure it looks the following:
```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("file:///<path-to-prototype-repo>/build/repo")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
    id("org.jetbrains.ecosystem").version("0.0.1-SNAPSHOT")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}


rootProject.name = "<project-name"
```
- You should remove Kotlin Gradle plugin versions if you define it in `build.gradle.kts`

Sync the project. If sync is successful – you could start using available project types. Note that could 
use `build.gradle.dcl` and `build.gradle.kts` files at the same time in different subprojects.
