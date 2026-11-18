plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
    api(project(":compiler-plugin:annotations"))

    compileOnly(libs.kotlin.compiler)
    compileOnly(libs.jetbrains.compose.runtime)
}

commonPublishing {
    configureDefaultJvmPublication()
}