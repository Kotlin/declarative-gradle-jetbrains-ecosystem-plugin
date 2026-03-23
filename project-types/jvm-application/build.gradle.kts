plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

description = "Implements 'jvmApplication' Declarative Gradle project type"

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
}

commonPublishing {
    configureDefaultJvmPublication()
}