plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
    api(libs.kotlin.gradle.plugin)

    implementation(libs.kotlin.gradle.plugin.serialization)
}

commonPublishing {
    configureDefaultJvmPublication()
}