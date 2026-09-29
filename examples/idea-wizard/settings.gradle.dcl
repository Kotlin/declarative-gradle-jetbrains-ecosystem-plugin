// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html
pluginManagement {
    includeBuild("../../")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
    id("org.jetbrains.ecosystem")
}

dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    repositories {
        mavenCentral()
    }
}

defaults {
    jvmApplication {
        toolchain {
            releaseVersion = 17
        }

        testing {
            useJUnitPlatform = true
        }
    }

    library {
        jvmPlatform {
            toolchain {
                releaseVersion = 17
            }
        }

        testing {
            jvmPlatform {
                useJUnitPlatform = true
            }
        }
    }
}

// Include the `app` and `utils` subprojects in the build.
// If there are changes in only one of the projects, Gradle will rebuild only the one that has changed.
// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
include(":app")
include(":utils")

rootProject.name = "idea-wizard"