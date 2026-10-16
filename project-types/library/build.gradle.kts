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
    implementation(libs.jetbrains.compose)
    implementation(libs.compose.compiler)
    api(project(":common"))
}

commonPublishing {
    configureDefaultJvmPublication()
}