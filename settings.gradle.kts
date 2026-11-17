pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "declarative-gradle-jetbrains-ecosystem-plugin"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
            }
        }
    }
}

include(
    ":ecosystem-plugin",
    ":common",
    ":project-types:jvm-application",
    ":project-types:web-application",
    ":project-types:library",
    ":project-types:desktop-application",
    ":spring",
    ":resource-packaging",
)
