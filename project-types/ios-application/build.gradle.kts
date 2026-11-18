plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
    id("xcode-project-parsing")
}

description = "Implements 'iosApplication' Declarative Gradle project type"

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
    api(project(":common"))

    implementation("org.jetbrains:xcode-project-file:1.0-SNAPSHOT")
    implementation(libs.jetbrains.compose)
    implementation(libs.compose.compiler)
    compileOnly(libs.jetbrains.compose.runtime)
    compileOnly(libs.kotlin.compiler)
}

commonPublishing {
    configureDefaultJvmPublication()
}