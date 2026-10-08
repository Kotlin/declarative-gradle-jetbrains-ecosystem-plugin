/*
 * `androidApp` is an official Android Gradle Plugin project type, see more here: https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:build-system/gradle-api/src/main/java/com/android/build/api/dsl/ApplicationDeclarativeDefinition.kt
 * To enable Declarative Gradle features in AGP, users must:
 * - Apply the Android ecosystem plugin (`id("com.android.ecosystem").version(...)`) in the project's `settings.gradle.dcl`
 * - Enable `android.experimental.declarative=true` in `gradle.properties`
 */
androidApp {
    compileSdk = 37
    namespace = "org.jetbrains.kmpwizard"
    enableKotlin = true

    defaultConfig {
        applicationId = "org.jetbrains.kmpwizard"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        buildType("release") {
            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                layout.projectDirectory.file("proguard-rules.pro"),
//            )
        }
        buildType("debug") {
            dependencies {
                implementation("org.jetbrains.compose.ui:ui-tooling:1.12.1")
            }
        }
    }

    compileOptions {
        sourceCompatibility = VERSION_17
        targetCompatibility = VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependencies {
        implementation(project(":shared"))
        implementation("androidx.activity:activity-compose:1.13.0")
        implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.12.1")
    }
}