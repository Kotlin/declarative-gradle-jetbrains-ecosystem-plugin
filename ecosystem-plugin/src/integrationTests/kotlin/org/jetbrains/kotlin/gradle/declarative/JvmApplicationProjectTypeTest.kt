package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName

@DisplayName("'jvmApplication' project type tests")
class JvmApplicationProjectTypeTest : BaseTest() {

    @DisplayName("project type is applied")
    @GradleTest
    fun testEcosystemPluginIsApplied(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            build("help") {
                assertOutputContains("JetbrainsEcosystemPlugin applied to settings")
            }
        }
    }

}