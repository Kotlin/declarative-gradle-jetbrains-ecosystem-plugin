library {
    platforms = listOf("jvm")

    kotlin {
        serialization {
            enabledFormats = listOf("json")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }

    testing {
        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test")
        }
    }
}