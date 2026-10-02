package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
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
                |    compose {
                |
                |    }           
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
}