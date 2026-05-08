package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.logging.LogLevel
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.BuildOptions
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

@DisplayName("Library project type")
class LibraryProjectTypeTest : BaseTest() {

    @DisplayName("smoke test")
    @GradleTest
    fun testSmoke(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "common", "web", "ios")
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("JetbrainsEcosystemPlugin applied to settings")
            }
        }
    }

    @DisplayName("Jvm only platform")
    @GradleTest
    fun testJvm(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("Enabling Kotlin/JVM plugin")
            }
        }
    }

    @DisplayName("Jvm and common platform")
    @GradleTest
    fun testJvmAndCommon(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "common")
                |}
                """.trimMargin()
            )

            build("help") {
                assertOutputContains("Enabling Kotlin/KMP plugin with 'jvm()' target")
            }
        }
    }

    @DisplayName("Add dependency to jvm project")
    @GradleTest
    fun testJvmAddDependencies(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |    
                |    dependencies {
                |        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${TestVersions.Dependencies.COROUTINES}")
                |        implementation("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
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

            build("compileKotlin") {
                assertTasksExecuted(":compileKotlin")
            }
        }
    }

    @DisplayName("Add platform dependency to jvm project")
    @GradleTest
    fun testAddJvmPlatformDependencies(gradleVersion: GradleVersion) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |    
                |    dependencies {
                |        jvmPlatform {
                |            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${TestVersions.Dependencies.COROUTINES}")
                |            implementation("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
                |        }
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

            build("compileKotlin") {
                assertTasksExecuted(":compileKotlin")
            }
        }
    }

    @DisplayName("Add common dependency")
    @GradleTest
    fun testAddCommonDependency(gradleVersion: GradleVersion) {
        project(
            "base-ecosystem-project",
            gradleVersion,
            buildOptions = defaultBuildOptions.copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED),
        ) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "web")
                |    
                |    dependencies {
                |        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${TestVersions.Dependencies.COROUTINES}")
                |        implementation("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
                |    }
                |}
                """.trimMargin()
            )

            setOf("jvmMain", "webMain").forEach {
                kotlinSourcesDir(it).source("com/example/coroutines.kt") {
                    //language=kotlin
                    """
                |package com.example
                |
                |import kotlinx.coroutines.*
                |import kotlinx.datetime.*
                |
                |suspend fun main() = coroutineScope {
                |    async { 
                |        println(DatePeriod().toString())
                |    }
                |}
                """.trimMargin()
                }
            }

            build(
                "compileKotlinJvm",
                "compileKotlinJs",
                "compileKotlinWasmJs",
            ) {
                assertTasksExecuted(
                    ":compileKotlinJvm",
                    ":compileKotlinJs",
                    ":compileKotlinWasmJs",
                )
            }
        }
    }

    @DisplayName("Add platform dependencies")
    @GradleTest
    fun testAddPlatformDependencies(gradleVersion: GradleVersion) {
        project(
            "base-ecosystem-project",
            gradleVersion,
            buildOptions = defaultBuildOptions.copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED),
        ) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "web")
                |    
                |    dependencies {
                |        jvmPlatform {
                |            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${TestVersions.Dependencies.COROUTINES}")
                |            implementation("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
                |        }
                |        
                |        webPlatform {
                |            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${TestVersions.Dependencies.COROUTINES}")
                |            implementation("org.jetbrains.kotlinx:kotlinx-datetime:${TestVersions.Dependencies.DATETIME}")
                |        }
                |    }
                |}
                """.trimMargin()
            )

            setOf("jvmMain", "webMain").forEach {
                kotlinSourcesDir(it).source("com/example/coroutines.kt") {
                    //language=kotlin
                    """
                |package com.example
                |
                |import kotlinx.coroutines.*
                |import kotlinx.datetime.*
                |
                |suspend fun main() = coroutineScope {
                |    async { 
                |        println(DatePeriod().toString())
                |    }
                |}
                """.trimMargin()
                }
            }

            build(
                "compileKotlinJvm",
                "compileKotlinJs",
                "compileKotlinWasmJs",
            ) {
                assertTasksExecuted(
                    ":compileKotlinJvm",
                    ":compileKotlinJs",
                    ":compileKotlinWasmJs",
                )
            }
        }
    }

    @DisplayName("it is possible configure JVM toolchain")
    @GradleTest
    fun testJvmToolchainConfiguration(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |    
                |    jvmPlatform {
                |        toolchain {
                |            releaseVersion = 21
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("assemble", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlin")
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
            }
        }
    }

    @DisplayName("Jvm-only: configure compiler options")
    @GradleTest
    fun testJvmCompilerOptions(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |    
                |    kotlin {
                |        compilerOptions {
                |            languageVersion = KOTLIN_2_2
                |            allWarningsAsErrors = true
                |        }
                |    }
                |    
                |    jvmPlatform {
                |        kotlin {
                |            compilerOptions {
                |                languageVersion = KOTLIN_2_3
                |                jvmDefault = ENABLE
                |            }
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("assemble", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlin")

                assertCompilerArgument(
                    ":compileKotlin",
                    "-language-version 2.3",
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

    @DisplayName("KMP: configure compiler options")
    @GradleTest
    fun testKmpCompilerOptions(
        gradleVersion: GradleVersion
    ) {
        project("base-ecosystem-project", gradleVersion) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "common")
                |    
                |    kotlin {
                |        compilerOptions {
                |            languageVersion = KOTLIN_2_2
                |            allWarningsAsErrors = true
                |        }
                |    }
                |    
                |    jvmPlatform {
                |        kotlin {
                |            compilerOptions {
                |                languageVersion = KOTLIN_2_3
                |                jvmDefault = ENABLE
                |            }
                |        }
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir("commonMain").source("src/kotlin/main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |fun main() {
                |   println("Hello")
                |}
                """.trimMargin()
            }

            build("assemble", "-Pkotlin.internal.compiler.arguments.log.level=warning") {
                assertTasksExecuted(":compileKotlinJvm")

                assertCompilerArgument(
                    ":compileKotlinJvm",
                    "-language-version 2.3",
                    logLevel = LogLevel.INFO,
                )
                assertCompilerArgument(
                    ":compileKotlinJvm",
                    "-Werror",
                    logLevel = LogLevel.INFO,
                )
                assertCompilerArgument(
                    ":compileKotlinJvm",
                    "-jvm-default=enable",
                    logLevel = LogLevel.INFO,
                )
            }
        }
    }
}