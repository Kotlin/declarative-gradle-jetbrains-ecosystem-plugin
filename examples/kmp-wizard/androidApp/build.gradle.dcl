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
//                "proguard-rules.pro"
//            )
        }
    }

    compileOptions {
        sourceCompatibility = VERSION_17
        targetCompatibility = VERSION_17
    }

    buildFeatures {
        compose = true
    }

    dependencies {
        implementation(project(":shared"))
        implementation("androidx.activity:activity-compose:1.13.0")
        implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.12.1")
//        debugImplementation(libs.compose.uiTooling)
    }
}