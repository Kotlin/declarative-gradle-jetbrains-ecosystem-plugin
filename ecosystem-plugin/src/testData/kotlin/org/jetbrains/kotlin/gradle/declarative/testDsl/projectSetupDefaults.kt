package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.api.initialization.resolve.RepositoriesMode
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Language("kts")
internal val DEFAULT_KOTLIN_SETTINGS_FILE =
    """
    pluginManagement {
        repositories {
            maven { url = uri("${System.getProperty("maven.repo.url")}") }
            mavenCentral()
            google()
            gradlePluginPortal()
        }
    }

    plugins {
        id("org.jetbrains.ecosystem").version("0.0.1-SNAPSHOT")
    }
    """.trimIndent()

@Suppress("UnstableApiUsage")
internal fun getKotlinDependencyManagementBlock(
    gradleRepositoriesMode: RepositoriesMode,
    localRepo: Path? = null,
): String =
    //language=kotlin
    """    
    |dependencyResolutionManagement {
    |    ${getKotlinRepositoryBlock(localRepo)}
    |    //repositoriesMode = ${mapRepositoryModeToString(gradleRepositoriesMode)}
    |}
    """.trimMargin()

internal fun getKotlinRepositoryBlock(
    localRepo: Path? = null,
): String =
    //language=kotlin
    """
    |
    |    repositories {
    |        mavenCentral()
    |        google()
    |        
    |        ${localRepo?.absolutePathString()?.let { repo -> "maven{ url = uri(\"${repo.replace("\\", "\\\\")}\") }" } ?: ""}
    |    }
    """.trimMargin()

@Suppress("UnstableApiUsage")
private fun mapRepositoryModeToString(gradleRepositoriesMode: RepositoriesMode): String {
    return when (gradleRepositoriesMode) {
        RepositoriesMode.PREFER_PROJECT -> "RepositoriesMode.PREFER_PROJECT"
        RepositoriesMode.PREFER_SETTINGS -> "RepositoriesMode.PREFER_SETTINGS"
        RepositoriesMode.FAIL_ON_PROJECT_REPOS -> "RepositoriesMode.FAIL_ON_PROJECT_REPOS"
    }
}
