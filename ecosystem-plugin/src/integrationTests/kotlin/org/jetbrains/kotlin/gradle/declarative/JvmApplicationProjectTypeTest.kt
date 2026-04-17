package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.logging.LogLevel
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.TestVersions
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertCompilerArgument
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.jdk21Info
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.writeText

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

    @DisplayName("it is possible configure JVM toolchain to compile and run the application")
    @GradleTest
    fun testJvmToolchainConfiguration(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainJdkKt"
                |    
                |    toolchain {
                |        releaseVersion = 21
                |    }
                |}
                """.trimMargin()
            )

            build("run", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlin", ":run")
                assertCompilerArgument(
                    ":compileKotlin",
                    "-jvm-target 21",
                    logLevel = LogLevel.INFO,
                )
                assertCompilerArgument(
                    ":compileKotlin",
                    "-jdk-home ${jdk21Info.javaHome.absolutePath}",
                    logLevel = LogLevel.INFO,
                )
                assertOutputContains("Hello, DCL! Using JDK: 21")
            }
        }
    }

    @DisplayName("possible to define dependencies")
    @GradleTest
    fun testConfigureApplicationDependencies(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    dependencies {
                |        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${TestVersions.Dependencies.COROUTINES}")
                |        compileOnly("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
                |        runtimeOnly("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir().source("com/example/coroutines.kt") {
                //language=kotlin
                """
                |package com.example
                |
                |import kotlinx.coroutines.*
                |import java.time.LocalDate
                |import kotlinx.datetime.*
                |
                |fun main() = runBlocking { 
                |   println("Hello, DCL!")
                |   val day = LocalDate(2020, 2, 21)
                |   val yearMonth: YearMonth = day.yearMonth    
                |}
                """.trimMargin()
            }

            build("run", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlin", ":run")
                assertOutputContains("Hello, DCL!")
            }
        }
    }
}