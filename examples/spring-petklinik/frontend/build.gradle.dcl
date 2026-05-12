webApplication {
    kotlin {
        serialization {
            enabledFormats = listOf("json")
        }
    }

    subplatforms = listOf("wasmJs")

    packaging {
        resource { }
    }

    dependencies {
        implementation(project(":shared"))
        implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        implementation("io.konform:konform:0.11.1")
    }
}
