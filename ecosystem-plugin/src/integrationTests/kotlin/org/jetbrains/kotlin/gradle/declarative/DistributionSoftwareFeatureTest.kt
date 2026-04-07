package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.*
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("Distribution Software Feature")
class DistributionSoftwareFeatureTest : BaseTest() {

    @DisplayName("creates default distribution software")
    @GradleTest
    fun testEcosystemPluginIsApplied(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                """
                |jvmApplication {
                |    packaging {
                |        distribution {
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("tasks") {
                assertOutputContains("Applying distribution software feature")

                assertOutputContains("Distribution tasks")
                assertOutputContains("assembleDist")
                assertOutputContains("distTar")
                assertOutputContains("distZip")
                assertOutputContains(DistributionPlugin.TASK_INSTALL_NAME)
            }
        }
    }
}