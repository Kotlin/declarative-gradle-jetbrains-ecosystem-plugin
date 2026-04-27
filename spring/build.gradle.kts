plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

description = "Implements 'spring' Declarative Gradle software feature"

kotlin {
    explicitApi()
}

dependencies {
    api(project(":project-types:jvm-application"))
    api(gradleApi())

    implementation(libs.spring.gradle.plugin.boot)
    implementation(libs.kotlin.gradle.plugin.allopen)
}

commonPublishing {
    configureDefaultJvmPublication()
}
