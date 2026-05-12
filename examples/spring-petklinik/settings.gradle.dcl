pluginManagement {
    includeBuild("../../")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
    id("org.jetbrains.ecosystem")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "Spring-PetKlinik"

defaults {
    jvmApplication {
        toolchain {
            releaseVersion = 17
        }

        spring { }

        packaging {
            spring {
                bootBuildImage {
                    environment += mapOf(
                        "BP_JVM_CDS_ENABLED" to "true",
                        "BP_JVM_VERSION" to "25"
                    )
                }
            }
        }

        testing {
            useJunitPlatform = true
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")
                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }
        }
    }

    library {
        platforms = listOf("jvm", "web")

        jvmPlatform {
            toolchain {
                releaseVersion = 17
            }
        }

        webPlatform {
            subplatforms = listOf("wasmJs")
        }

        kotlin {
            serialization {
                enabledFormats = listOf("json")
            }
        }
    }
}

include("frontend")
include("backend")
include("shared")
include("ai")
