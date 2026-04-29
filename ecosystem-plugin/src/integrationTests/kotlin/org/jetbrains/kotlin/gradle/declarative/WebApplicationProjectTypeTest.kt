package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.project.IsolatedProject
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.BuildOptions
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
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

            build("jsBrowserDevelopmentExecutableDistribution", forwardBuildOutput = true) {
                assertTasksExecuted(":jsBrowserDevelopmentExecutableDistribution")
            }
        }
    }
}