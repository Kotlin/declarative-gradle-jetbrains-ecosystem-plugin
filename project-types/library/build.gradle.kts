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
    implementation(libs.compose.compiler)
    api(project(":common"))
}

commonPublishing {
    configureDefaultJvmPublication()
}