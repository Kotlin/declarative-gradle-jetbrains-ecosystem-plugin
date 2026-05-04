package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

@DisplayName("Resources software feature test")
class ResourcesPackagingSoftwareFeatureTest : BaseTest() {

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
}