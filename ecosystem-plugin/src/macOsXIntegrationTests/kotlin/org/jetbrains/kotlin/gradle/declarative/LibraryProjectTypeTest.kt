package org.jetbrains.kotlin.gradle.declarative

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.testDsl.BaseTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.GradleTest
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertOutputContains
import org.jetbrains.kotlin.gradle.declarative.testDsl.assertTasksExecuted
import org.jetbrains.kotlin.gradle.declarative.testDsl.build
import org.jetbrains.kotlin.gradle.declarative.testDsl.project
import org.jetbrains.kotlin.gradle.declarative.testDsl.source
import org.junit.jupiter.api.DisplayName
import kotlin.io.path.appendText
import kotlin.io.path.writeText

@DisplayName("Library project type")
class LibraryProjectTypeTest : BaseTest() {

    @DisplayName("iOS framework publishing")
    @GradleTest
    fun testIosFramework(gradleVersion: GradleVersion) {
        project("ios-framework-project", gradleVersion) {
            subProject("exported").run {
                buildGradleDcl.writeText(
                    //language=declarative
                    """
                    |library {
                    |    platforms = listOf("ios")
                    |    
                    |    iosPlatform {
                    |        subplatforms = listOf("iosArm64")
                    |    }
                    |    
                    |    publishing {
                    |        iosFramework {
                    |            static = true
                    |        }
                    |    }
                    |}
                    """.trimMargin()
                )

                kotlinSourcesDir("iosMain").source("main.kt") {
                    //language=kotlin
                    """
                    |package org.example
                    |
                    |fun foo(): Unit = println("Exported!")
                    """.trimMargin()
                }
            }

            buildGradleDcl.writeText(
                //language=declarative
                """
                |library {
                |    platforms = listOf("ios")
                |    
                |    iosPlatform {
                |        subplatforms = listOf("iosArm64")
                |    }
                |    
                |    dependencies {
                |        api(project(":exported"))
                |    }
                |    
                |    publishing {
                |        iosFramework {
                |            static = true
                |            export(project(":exported"))
                |            transitiveExport = true
                |        }
                |    }
                |}
                """.trimMargin()
            )

            settingsGradleDcl.appendText(
                //language=declarative
                """
                |
                |rootProject.name = "ios-framework-project"
                |include("exported")
                """.trimMargin()
            )

            kotlinSourcesDir("iosMain").source("main.kt") {
                //language=kotlin
                """
                |package org.example
                |
                |data class Project(val name: String, val language: String)
                |
                |fun main() {
                |    println(foo())
                |    println(Project("test", "Kotlin"))
                |}
                """.trimMargin()
            }

            build("build") {
                assertTasksExecuted(":build")
                assertOutputContains("-Xexport-library=.*/ios-framework-project/exported/build/classes/kotlin/iosArm64/main/klib/exported".toRegex())
                assertOutputContains("build/bin/iosArm64/releaseFramework/ios_framework_project.framework")
            }
        }
    }
}