package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.BuildOptions
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText
import kotlin.test.assertTrue

@DisplayName("Resources software feature test")
class ResourcesPackagingSoftwareFeatureTest : BaseTest() {

    override val defaultBuildOptions: BuildOptions
        get() = super.defaultBuildOptions.copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED)

    @DisplayName("Exposes as consumable configuration")
    @GradleTest
    fun testExposePackaged(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |webApplication {
                |    packaging {
                |        resource {}
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

            build("outgoingVariants") {
                assertOutputContains("Variant packaged-as-resources")
            }
        }
    }

    @DisplayName("Consumes into resources")
    @GradleTest
    fun testConsumePackagedResources(gradleVersion: GradleVersion) {
        project("multi-module-project", gradleVersion) {
            val webAppProject = subProject("web-app")
            webAppProject.buildGradleDcl.writeText(
                //language=declarative
                """
                |webApplication {
                |    packaging {
                |        resource {}
                |    }
                |}
                """.trimMargin()
            )
            val jvmAppProject = subProject("jvm-app")
            jvmAppProject.buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    dependencies {
                |        resources {
                |            resource(project(":web-app"))
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("processResources") {
                assertTasksExecuted(":jvm-app:processResources")
                val staticDir = jvmAppProject.projectPath.resolve("build/resources/main/static")
                val files = staticDir.listDirectoryEntries()
                assertTrue(files.isNotEmpty())
            }
        }
    }
}