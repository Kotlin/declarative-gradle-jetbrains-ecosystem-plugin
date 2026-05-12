package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.BuildOptions
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.assertTrue

@DisplayName("maven {} publishing software feature")
class MavenPublishingSoftwareFeatureTest : BaseTest() {

    @DisplayName("publishing tasks are created")
    @GradleTest
    fun testPublishingTasksAreCreated(gradleVersion: GradleVersion) {
        project(
            "base-ecosystem-project",
            gradleVersion,
        ) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |    
                |    publishing {
                |        maven {
                |            name = "local"
                |            repositoryUrl = "file:///tmp"
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("tasks") {
                assertOutputContains("publish -")
                assertOutputContains("publishLocalPublicationToLocalRepository - ")
            }
        }
    }

    @DisplayName("actual publishing works in jvm libraries")
    @GradleTest
    fun testPublishingJvm(
        gradleVersion: GradleVersion,
        @TempDir tempDir: Path,
    ) {
        project(
            "base-ecosystem-project",
            gradleVersion,
        ) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm")
                |    
                |    publishing {
                |        group = "org.example"
                |        version = "1.0.0"
                |        
                |        maven {
                |            name = "local"
                |            repositoryUrl = "file://${tempDir.absolutePathString()}"
                |            withDocs = true
                |            withSources = true
                |        }
                |    }
                |}
                """.trimMargin()
            )

            build("publishLocalPublicationToLocalRepository") {
                val rootPublicationDir = tempDir.resolve("org/example/base-ecosystem-project")
                assertTrue(rootPublicationDir.exists())
                assertTrue(rootPublicationDir.resolve("1.0.0").exists())
                assertTrue(rootPublicationDir.resolve("1.0.0/base-ecosystem-project-1.0.0.jar").exists())
                assertTrue(rootPublicationDir.resolve("1.0.0/base-ecosystem-project-1.0.0-sources.jar").exists())
                assertTrue(rootPublicationDir.resolve("1.0.0/base-ecosystem-project-1.0.0-javadoc.jar").exists())
            }
        }
    }

    @DisplayName("actual publishing works in kmp libraries")
    @GradleTest
    fun testPublishingKmp(
        gradleVersion: GradleVersion,
        @TempDir tempDir: Path,
    ) {
        project(
            "base-ecosystem-project",
            gradleVersion,
            buildOptions = defaultBuildOptions.copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED)
        ) {
            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("jvm", "web")
                |    
                |    publishing {
                |        group = "org.example"
                |        version = "1.0.0"
                |        
                |        maven {
                |            name = "local"
                |            repositoryUrl = "file://${tempDir.absolutePathString()}"
                |            withDocs = true
                |            withSources = true
                |        }
                |    }
                |}
                """.trimMargin()
            )

            kotlinSourcesDir("commonMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |fun main() {
                |    println("hello")
                |}
                """.trimMargin()
            }

            fun Path.assertPublicationExists(
                publicationName: String = "base-ecosystem-project",
                isKlib: Boolean = false,
            ) {
                val fileExtension = if (isKlib) "klib" else "jar"
                assertTrue(exists())
                assertTrue(resolve("$publicationName-1.0.0.$fileExtension").exists())
                assertTrue(resolve("$publicationName-1.0.0-sources.jar").exists())
            }

            build("publishAllPublicationsToLocalRepository") {
                tempDir.resolve("org/example/base-ecosystem-project/1.0.0").assertPublicationExists()
                tempDir.resolve("org/example/base-ecosystem-project-jvm/1.0.0")
                    .assertPublicationExists("base-ecosystem-project-jvm")
                tempDir.resolve("org/example/base-ecosystem-project-js/1.0.0")
                    .assertPublicationExists("base-ecosystem-project-js", isKlib = true)
                tempDir.resolve("org/example/base-ecosystem-project-wasm-js/1.0.0")
                    .assertPublicationExists("base-ecosystem-project-wasm-js", isKlib = true)
            }
        }
    }
}