library {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0")
        implementation("io.konform:konform:0.11.1")

        webPlatform {
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }

    testing {
        webPlatform {
            skip = true
        }

        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test")

            jvmPlatform {
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
            webPlatform {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
            }
        }
    }
}
