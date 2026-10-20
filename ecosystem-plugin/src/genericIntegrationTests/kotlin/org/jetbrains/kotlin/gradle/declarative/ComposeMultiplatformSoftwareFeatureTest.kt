package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.TestVersions
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("`compose {}` Software Feature")
class ComposeMultiplatformSoftwareFeatureTest : BaseTest() {

    @DisplayName("compose plugins are applied")
    @GradleTest
    fun testComposeMultiplatformPluginsAreApplied(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf(jvm, common)
                |    
                |    compose { }           
                |}
                |
                """.trimMargin()
            )

            build("build") {
                assertOutputContains(
                    """
                    |Configure multi-module compose resources
                    |Configure compose resources generation
                    |Configure Res object generation for commonMain
                    |Configure resource accessors generation for commonMain
                    |Configure resource accessors generation for commonTest
                    |Configure resource accessors generation for jvmMain
                    |Configure resource accessors generation for jvmTest
                    |Configure expect resource collectors generation for commonMain
                    |Configure actual resource collectors generation for jvmMain
                    |Configure main resources for 'jvm' target
                    |Configure test resources for 'jvm' target
                    |Check that new 'org.jetbrains.kotlin.plugin.compose' was applied
                    |Configure resource accessors generation for jvmDev
                    |.*
                    |Configure dev resources for 'jvm' target
                    """.trimMargin().toRegex(RegexOption.MULTILINE)
                )
            }
        }
    }

    @DisplayName("Support for Android application project type")
    @GradleTest
    fun testComposeMultiplatformPluginsWithAndroid(gradleVersion: GradleVersion) {
        project("android-app-project", gradleVersion) {
            settingsGradleDcl.writeText(
                //language=declarative
                """
                |pluginManagement {
                |    repositories {
                |        mavenCentral()
                |        google()
                |        gradlePluginPortal()
                |        maven { url = uri("${System.getProperty("maven.repo.url")}") }
                |        maven {
                |            url = uri("https://androidx.dev/studio/builds/${TestVersions.Android.NIGHTLY_BUILD}/artifacts/artifacts/repository")
                |        }
                |    }
                |}
                |
                |plugins {
                |    id("org.jetbrains.ecosystem").version("0.0.1-SNAPSHOT")
                |    id("com.android.ecosystem").version("${TestVersions.Android.ECOSYSTEM_PLUGIN}")
                |}
                |
                |dependencyResolutionManagement {
                |    repositories {
                |        mavenCentral()
                |        google()
                |        maven {
                |            url = uri("https://androidx.dev/studio/builds/${TestVersions.Android.NIGHTLY_BUILD}/artifacts/artifacts/repository")
                |        }
                |    }
                |}
                """.trimMargin()
            )

            buildGradleDcl.writeText(
                //language=declarative
                """
                |androidApp {
                |    namespace = "org.example.app"
                |    compileSdk = 34
                |    
                |    compose { }       
                |
                |    compileOptions {
                |        sourceCompatibility = VERSION_17
                |        targetCompatibility = VERSION_17
                |    }
                |
                |    defaultConfig {
                |        minSdk = 30
                |        versionCode = 1
                |        versionName = "0.1"
                |        applicationId = "org.jetbrains.declarative.gradle.android.app"
                |    }
                |}
                """.trimMargin()
            )

            build("build") {
                assertTasksExecuted(":build")
            }
        }
    }
}