plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("common-publishing")
}

kotlin {
    jvm()
    iosArm64()
    iosX64()

    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs = listOf("-Xexpect-actual-classes")
        optIn = listOf("kotlin.ExperimentalMultiplatform")
    }
}