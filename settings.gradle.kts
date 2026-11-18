pluginManagement {
    includeBuild("gradle/build-logic")
    includeBuild("project-types/ios-application/xcode-project-parsing")
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
        mavenLocal()
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
    ":compiler-plugin",
    ":compiler-plugin:annotations",
    ":common",
    ":project-types:jvm-application",
    ":project-types:web-application",
    ":project-types:library",
    ":project-types:ios-application",
    ":spring",
    ":resource-packaging",
)