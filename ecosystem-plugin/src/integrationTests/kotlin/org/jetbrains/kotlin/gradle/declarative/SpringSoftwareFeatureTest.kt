package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksFailed
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.buildAndFail
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("Spring software feature")
class SpringSoftwareFeatureTest : BaseTest() {

    @DisplayName("Is applied")
    @GradleTest
    fun testSmoke(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    spring {}
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("Applying Spring software feature to project")
            }
        }
    }

    @DisplayName("Runnable as spring app")
    @GradleTest
    fun testSpringApplication(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    spring {}
                |}
                """.trimMargin()
            )

            build("bootRun") {
                assertTasksExecuted(":bootRun")
            }
        }
    }

    @DisplayName("Add Spring dependencies")
    @GradleTest
    fun testSpringDependencies(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    spring {}
                |    
                |    dependencies {
                |        spring {
                |             developmentOnly("org.springframework.boot:spring-boot-docker-compose")
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("bootRun") {
                assertTasksExecuted(":bootRun")
            }
        }
    }

    @DisplayName("Add Spring packaging configuration")
    @GradleTest
    fun testSpringPackaging(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    spring {}
                |    
                |    packaging {
                |        spring {
                |             bootBuildImage {
                |                 environment = mapOf("BP_JVM_CDS_ENABLED" to "true")
                |             }
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("tasks") {
                // This task requires Docker to run
                //assertTasksExecuted(":bootBuildImage")
            }
        }
    }
}