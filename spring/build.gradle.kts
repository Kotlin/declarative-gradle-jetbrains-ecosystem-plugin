plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

description = "Implements 'spring' Declarative Gradle software feature"

kotlin {
    explicitApi()
}

dependencies {
    api(project(":common"))
    api(gradleApi())

    implementation(libs.spring.gradle.plugin.boot)
}

commonPublishing {
    configureDefaultJvmPublication()
}
