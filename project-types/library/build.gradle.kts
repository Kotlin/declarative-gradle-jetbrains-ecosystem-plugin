plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

description = "Implements 'library' Declarative Gradle project type"

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
    api(libs.android.kotlin.multiplatform)
    api(project(":common"))
}

commonPublishing {
    configureDefaultJvmPublication()
}