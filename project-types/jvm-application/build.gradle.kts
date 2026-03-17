plugins {
    alias(libs.plugins.kotlin.jvm)
}

description = "Implements 'jvmApplication' Declarative Gradle project type"

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
}