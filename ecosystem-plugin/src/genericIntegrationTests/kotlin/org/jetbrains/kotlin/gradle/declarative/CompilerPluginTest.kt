package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.buildAndFail
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

//TODO: turn these into compiler tests at some point
@DisplayName("Tests for Declarative Gradle Compiler Plugin")
class CompilerPluginTest : BaseTest() {

    @DisplayName("Declarative Gradle Compiler Plugin is applied")
    @GradleTest
    fun `Declarative Gradle Compiler Plugin is applied`(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            settingsGradleDcl
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf(ios)
                |    
                |    iosPlatform {
                |        subplatforms = listOf(iosArm64)
                |    }
                |    
                |    dependencies {
                |        implementation("org.jetbrains.compose.ui:ui:+")
                |    }
                |}
                """.trimMargin()
            )
            kotlinSourcesDir("iosMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import platform.UIKit.UIViewController
                |import androidx.compose.ui.window.ComposeUIViewController
                |import org.jetbrains.kotlin.gradle.declarative.compiler.annotations.IosApplicationEntryPoint
                |
                |@IosApplicationEntryPoint
                |fun ViewController() = ComposeUIViewController { TODO() }
                """.trimMargin()
            }
            build("compileKotlinIosArm64") {
                assertTasksExecuted(":compileKotlinIosArm64")
            }
        }
    }

    @DisplayName("@IosApplicationEntryPoint applied to a function not at top-level")
    @GradleTest
    fun `@IosApplicationEntryPoint applied to a function not at top-level`(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            settingsGradleDcl
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf(ios)
                |    
                |    iosPlatform {
                |        subplatforms = listOf(iosArm64)
                |    }
                |    
                |    dependencies {
                |        implementation("org.jetbrains.compose.ui:ui:+")
                |    }
                |}
                """.trimMargin()
            )
            kotlinSourcesDir("iosMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import platform.UIKit.UIViewController
                |import androidx.compose.ui.window.ComposeUIViewController
                |import org.jetbrains.kotlin.gradle.declarative.compiler.annotations.IosApplicationEntryPoint
                |
                |object ViewControllerProvider {
                |    @IosApplicationEntryPoint
                |    fun ViewController() = ComposeUIViewController { TODO() }
                |}
                |
                """.trimMargin()
            }
            buildAndFail("compileKotlinIosArm64") {
                assertOutputContains("@IosApplicationEntryPoint is only applicable to top-level functions.")
            }
        }
    }

    @DisplayName("@IosApplicationEntryPoint applied to a non-public function")
    @GradleTest
    fun `@IosApplicationEntryPoint applied to a non-public function`(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            settingsGradleDcl
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf(ios)
                |    
                |    iosPlatform {
                |        subplatforms = listOf(iosArm64)
                |    }
                |    
                |    dependencies {
                |        implementation("org.jetbrains.compose.ui:ui:+")
                |    }
                |}
                """.trimMargin()
            )
            kotlinSourcesDir("iosMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import platform.UIKit.UIViewController
                |import androidx.compose.ui.window.ComposeUIViewController
                |import org.jetbrains.kotlin.gradle.declarative.compiler.annotations.IosApplicationEntryPoint
                |
                |@IosApplicationEntryPoint
                |private fun ViewController() = ComposeUIViewController { TODO() }
                """.trimMargin()
            }
            buildAndFail("compileKotlinIosArm64") {
                assertOutputContains("Functions annotated with @IosApplicationEntryPoint must be public.")
            }
        }
    }

    @DisplayName("@IosApplicationEntryPoint applied to a function returning UIViewController non-explicitly")
    @GradleTest
    fun `@IosApplicationEntryPoint applied to a function returning UIViewController non-explicitly`(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            settingsGradleDcl
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf(ios)
                |    
                |    iosPlatform {
                |        subplatforms = listOf(iosArm64)
                |    }
                |    
                |    dependencies {
                |        implementation("org.jetbrains.compose.ui:ui:+")
                |    }
                |}
                """.trimMargin()
            )
            kotlinSourcesDir("iosMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import platform.UIKit.UIViewController
                |import androidx.compose.ui.window.ComposeUIViewController
                |import org.jetbrains.kotlin.gradle.declarative.compiler.annotations.IosApplicationEntryPoint
                |
                |@IosApplicationEntryPoint
                |fun ViewController(): Any = ComposeUIViewController { TODO() }
                """.trimMargin()
            }
            buildAndFail("compileKotlinIosArm64") {
                assertOutputContains("Functions annotated with @IosApplicationEntryPoint must explicitly return an UIViewController instance.")
            }
        }
    }
}