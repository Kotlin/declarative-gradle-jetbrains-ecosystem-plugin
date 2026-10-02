pluginManagement {
    includeBuild("../../")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://androidx.dev/studio/builds/16397002/artifacts/artifacts/repository")
        }
    }
}

plugins {
    id("org.jetbrains.ecosystem")
    id("com.android.ecosystem").version("9.5.0-dev")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://androidx.dev/studio/builds/16397002/artifacts/artifacts/repository")
        }
    }
}

rootProject.name = "kmp-wizard"

include(":androidApp")
include(":shared")