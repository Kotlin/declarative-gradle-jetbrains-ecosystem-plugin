package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("iOS application project type")
class IosApplicationProjectTypeTest : BaseTest() {

    @DisplayName("smoke test")
    @GradleTest
    fun testSmoke(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |iosApplication {
                |    subplatforms = listOf(iosArm64, iosX64)
                |    
                |    xcodeProject {
                |        entryPoint = auto
                |    }
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("JetbrainsEcosystemPlugin applied to settings")
                println(output)
            }
        }
    }
}