plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
}

commonPublishing {
    configureDefaultJvmPublication()
}