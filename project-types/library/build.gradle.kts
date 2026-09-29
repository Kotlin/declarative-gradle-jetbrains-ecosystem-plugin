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
    compileOnly(libs.android.kotlin.multiplatform)
    implementation(libs.jetbrains.compose)
    api(project(":common"))
}

commonPublishing {
    configureDefaultJvmPublication()
}