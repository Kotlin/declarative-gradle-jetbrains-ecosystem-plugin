package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName

@DisplayName("'jvmApplication' project type")
class JvmApplicationProjectTypeTest : BaseTest() {

    @DisplayName("could be applied without any additional configuration")
    @GradleTest
    fun testSmoke(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            build("help") {
                assertOutputContains("JetbrainsEcosystemPlugin applied to settings")
            }
        }
    }

    @DisplayName("default application is runnable")
    @GradleTest
    fun testDefaultApplication(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {

            build("tasks") {
                assertOutputContains("Application tasks")
                assertOutputContains("run - Runs this project as a JVM application")
            }

            build("run") {
                assertTasksExecuted(":compileKotlin", ":run")
                assertOutputContains("Hello, DCL!")
            }
        }
    }
}