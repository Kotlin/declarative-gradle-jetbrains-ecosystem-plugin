package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("Library project type")
class LibraryProjectTypeTest : BaseTest() {

    @DisplayName("smoke test")
    @GradleTest
    fun testSmoke(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "common", "web", "ios")
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("JetbrainsEcosystemPlugin applied to settings")
            }
        }
    }

    @DisplayName("Jvm only platform")
    @GradleTest
    fun testJvm(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("Enabling Kotlin/JVM plugin")
            }
        }
    }

    @DisplayName("Jvm and common platform")
    @GradleTest
    fun testJvmAndCommon(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "common")
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("Enabling Kotlin/KMP plugin with 'jvm()' target")
            }
        }
    }
}