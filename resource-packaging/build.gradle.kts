plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

description = "Implements 'resource' Declarative Gradle software feature"

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
    api(project(":common"))
}

commonPublishing {
    configureDefaultJvmPublication()
}
