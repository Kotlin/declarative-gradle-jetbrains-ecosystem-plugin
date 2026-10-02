library {
    platforms = listOf("android", "ios")
    enableCompose = true    //TODO: temporary solution

    iosPlatform {
        subplatforms = listOf("iosArm64", "iosSimulatorArm64")
        kotlin {
            compilerOptions {
                moduleName = "org.jetbrains.kmpwizard.shared"
            }
        }
    }

    androidPlatform {
        namespace = "org.jetbrains.kmpwizard.shared"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget = JVM_11
        }

        androidResources {
            enable = true
        }
    }

    testing {
        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test:2.4.20")
        }

        androidPlatform {
            hostTest {
                includeAndroidResources = true
            }

            deviceTest {
                sourceSetTreeName = "test"
                instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }

    dependencies {
        implementation("org.jetbrains.compose.runtime:runtime:1.12.1")
        implementation("org.jetbrains.compose.foundation:foundation:1.12.1")
        implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")
        implementation("org.jetbrains.compose.ui:ui:1.12.1")
        implementation("org.jetbrains.compose.components:components-resources:1.12.1")
        implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.12.1")
        implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
        implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

        androidPlatform {
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.12.1")
            implementation("org.jetbrains.compose.ui:ui-tooling:1.12.1")
            runtimeClasspath("org.jetbrains.compose.ui:ui-tooling:1.12.1")
        }
    }

    //TODO: Add iOS framework publishing once PR #81 is merged
}