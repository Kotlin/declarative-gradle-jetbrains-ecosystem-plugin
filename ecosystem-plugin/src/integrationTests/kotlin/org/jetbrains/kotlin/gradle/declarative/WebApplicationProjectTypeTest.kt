package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.logging.LogLevel
import org.gradle.api.project.IsolatedProject
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.BuildOptions
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertCompilerArgument
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.buildAndFail
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("Web application project type")
class WebApplicationProjectTypeTest : BaseTest() {

    override val defaultBuildOptions: BuildOptions
        get() = super.defaultBuildOptions.copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED)

    @DisplayName("smoke")
    @GradleTest
    fun testSmoke(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |webApplication {
                |}
                """.trimMargin()
            )

            build("help")
        }
    }

    @DisplayName("compile Browser code")
    @GradleTest
    fun testCompilerBrowserCode(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |webApplication {
                |    dependencies {
                |         implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir("jsMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import kotlinx.browser.document
                |
                |fun main() {
                |    println("Hello, web application!")
                |    document.bgColor = "FFAA12"
                |}
                """.trimMargin()
            }

            build("jsBrowserDevelopmentExecutableDistribution") {
                assertTasksExecuted(":jsBrowserDevelopmentExecutableDistribution")
            }
        }
    }

    @DisplayName("setting Kotlin compile options")
    @GradleTest
    fun testKotlinCompilerOptions(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |webApplication {
                |    kotlin {
                |        compilerOptions {
                |            allWarningsAsErrors = true
                |            sourceMap = true
                |        }
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir("jsMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |fun main() {
                |    println("Hello, web application!")
                |}
                """.trimMargin()
            }

            build("jsBrowserDevelopmentExecutableDistribution") {
                assertTasksExecuted(":jsBrowserDevelopmentExecutableDistribution")

                assertCompilerArgument(
                    ":compileKotlinJs",
                    expectedArgument = "-Werror",
                    logLevel = LogLevel.INFO
                )
                assertCompilerArgument(
                    ":compileKotlinJs",
                    expectedArgument = "-source-map",
                    logLevel = LogLevel.INFO
                )
            }
        }
    }

    @DisplayName("enable Kotlin serialization")
    @GradleTest
    fun testKotlinSerializationFeature(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |webApplication {
                |    kotlin {
                |        serialization {
                |            enabledFormats = listOf("json")
                |        }
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir("jsMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import kotlinx.serialization.*
                |import kotlinx.serialization.json.*
                |
                |@Serializable 
                |data class Project(val name: String, val language: String)
                |
                |fun main() {
                |    println("Hello, web application!")
                |    
                |    val data = Project("kotlinx.serialization", "Kotlin")
                |    val string = Json.encodeToString(data)  
                |}
                """.trimMargin()
            }

            build("compileKotlinJs") {
                assertTasksExecuted(":compileKotlinJs")
            }
        }
    }
}