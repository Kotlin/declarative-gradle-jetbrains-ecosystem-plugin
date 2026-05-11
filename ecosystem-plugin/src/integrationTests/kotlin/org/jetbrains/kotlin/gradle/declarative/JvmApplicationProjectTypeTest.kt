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
import org.jetbrains.kotlin.gradle.declarative.testDsl.buildAndFail
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

    @DisplayName("possible to configure java compiler arguments")
    @GradleTest
    fun testConfigureJavaCompilerArguments(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    java {
                |        compilerOptions {
                |            compilerArgs = listOf("-Xlint:empty")
                |        }
                |    }
                |}
                """.trimMargin()
            )

            javaSourcesDir().source("org/example/LintExample.java") {
                //language=java
                """
                |package org.example;
                |
                |class LintExample {
                |    /**
                |     * This method demonstrates how javac's -Xlint:empty works. Note that javac's
                |     * -Xlint:empty will only flag the empty statement involved in the "if" block,
                |     * but does not flag the empty statements associated with the do-while loop,
                |     * the while loop, the for loop, or the if-else. NetBeans does flag these if
                |     * the appropriate "Hints" are turned on.
                |     */
                |    private static void demonstrateEmptyWarning() {
                |       int[] integers = {1, 2, 3, 4, 5};
                |       if (integers.length != 5);
                |          System.out.println("Not five?");
                |    
                |       if (integers.length == 5)
                |          System.out.println("Five!");
                |       else;
                |          System.out.println("Not Five!");
                |    
                |       do;
                |       while (integers.length > 0);
                |    
                |       for (int integer : integers);
                |          System.out.println("Another integer found!");
                |    
                |       int counter = 0;
                |       while (counter < 5);
                |    
                |       System.out.println("Extra semicolons.");;;;
                |    }
                |}
                """.trimMargin()
            }

            build("run") {
                assertTasksExecuted(":compileKotlin", ":compileJava", ":run")
                assertOutputContains("Hello, DCL!")
                assertOutputContains("warning: [empty] empty statement after if")
            }
        }
    }

    @DisplayName("it is possible to configure Kotlin compilation options")
    @GradleTest
    fun testKotlinCompilerOptions(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.MainKt"
                |    
                |    kotlin {
                |        compilerOptions {
                |            languageVersion = KOTLIN_2_2
                |            apiVersion = KOTLIN_2_2
                |            allWarningsAsErrors = true
                |            jvmDefault = ENABLE
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("run", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlin", ":run")
                assertOutputContains("Hello, DCL!")

                assertCompilerArgument(
                    ":compileKotlin",
                    "-api-version 2.2",
                    logLevel = LogLevel.INFO,
                )
                assertCompilerArgument(
                    ":compileKotlin",
                    "-language-version 2.2",
                    logLevel = LogLevel.INFO,
                )
                assertCompilerArgument(
                    ":compileKotlin",
                    "-Werror",
                    logLevel = LogLevel.INFO,
                )
                assertCompilerArgument(
                    ":compileKotlin",
                    "-jvm-default=enable",
                    logLevel = LogLevel.INFO,
                )
            }
        }
    }

    @DisplayName("kotlin serialization is enabled")
    @GradleTest
    fun testKotlinSerializationSoftwareFeature(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.SerializationKt"
                |    
                |    kotlin {
                |        serialization {
                |            enabledFormats = listOf("json")
                |        }
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir().source("org/example/serialization.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import kotlinx.serialization.*
                |import kotlinx.serialization.json.*
                |
                |@Serializable 
                |data class Project(val name: String, val language: String)
                |
                |fun main() {
                |    val data = Project("kotlinx.serialization", "Kotlin")
                |    val string = Json.encodeToString(data)  
                |    println(string)
                |    val obj = Json.decodeFromString<Project>(string)
                |    println(obj)
                |}
                """.trimMargin()
            }

            build("run", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlin", ":run")
            }
        }
    }

    @DisplayName("testing is possible to configure")
    @GradleTest
    fun testTestingConfiguration(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |jvmApplication {
                |    mainClass = "org.example.SerializationKt"
                |    
                |    testing {
                |        useJunitPlatform = true
                |        dependencies {
                |            implementation(platform("org.junit:junit-bom:5.14.3"))
	            |            implementation("org.junit.jupiter:junit-jupiter")
	            |            runtimeOnly("org.junit.platform:junit-platform-launcher")
                |        }
                |    } 
                |}
                """.trimMargin()
            )

            kotlinSourcesDir("test").source("org/example/Test.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |import org.junit.jupiter.api.Test
                |import org.junit.jupiter.api.Assertions.assertEquals
                |
                |class OneTest {
                |     @Test
                |     fun testOne() {
                |          assertEquals(1, 0 + 1)
                |     }    
                |}
                """.trimMargin()
            }

            build("test") {
                assertTasksExecuted(":compileKotlin", ":compileTestKotlin", ":test")
            }
        }
    }
}