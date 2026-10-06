package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.buildAndFail
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("Ecosystem Plugin Test")
class EcosystemPluginTest : BaseTest() {

    @DisplayName("Gradle version mismatch")
    @GradleTest
    fun testGradleVersionMismatch(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", GradleVersion.version("9.6.0")) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |}
                """.trimMargin()
            )

            buildAndFail("help") {
                assertOutputContains("Plugin org.jetbrains.ecosystem:ecosystem-plugin:.* requires at least Gradle ${gradleVersion.version}.".toRegex())
            }
        }
    }
}