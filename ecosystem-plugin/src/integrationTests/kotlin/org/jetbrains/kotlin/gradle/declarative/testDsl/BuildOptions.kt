package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.LoggingConfigurationBuildOptions.StacktraceOption
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.condition.OS
import java.util.*

val DEFAULT_LOG_LEVEL = LogLevel.INFO

data class BuildOptions(
    val logLevel: LogLevel = DEFAULT_LOG_LEVEL,
    val stacktraceMode: String? = StacktraceOption.FULL_STACKTRACE_LONG_OPTION,
    val kotlinVersion: String = TestVersions.Kotlin.CURRENT,
    val configurationCache: ConfigurationCacheValue = ConfigurationCacheValue.ENABLED,
    val isolatedProjects: IsolatedProjectsMode = IsolatedProjectsMode.ENABLED,
    val configurationCacheProblems: ConfigurationCacheProblems = ConfigurationCacheProblems.FAIL,
    val parallel: Boolean = true,
    val maxWorkers: Int = (Runtime.getRuntime().availableProcessors() / 4 - 1).coerceAtLeast(2),
    /**
     * Enable File System Watching
     *
     * Disabled by default on Windows OS because enabling watch-fs prevents deleting the temp directory,
     * which fails the tests.
     *
     * See https://docs.gradle.org/current/userguide/file_system_watching.html
     */
    val fileSystemWatchEnabled: Boolean = !OS.WINDOWS.isCurrentOs,
    val buildCacheEnabled: Boolean = false,
    val compilerArgumentsLogLevel: String? = "info",
) {
    enum class ConfigurationCacheValue {

        /** Explicitly/forcefully disable Configuration Cache */
        DISABLED,

        /** Explicitly/forcefully enable Configuration Cache */
        ENABLED,

        /** Gradle, depending on its version, will decide whether to enable Configuration Cache */
        UNSPECIFIED;

        fun toBooleanFlag(gradleVersion: GradleVersion): Boolean? = when (this) {
            DISABLED -> false
            ENABLED -> true
            UNSPECIFIED -> null
        }
    }

    enum class IsolatedProjectsMode {

        /** Always disable Isolated Projects */
        DISABLED,

        /** Always enable Isolated Projects */
        ENABLED;

        fun toBooleanFlag(gradleVersion: GradleVersion) = when (this) {
            DISABLED -> false
            ENABLED -> true
        }
    }

    fun toArguments(
        gradleVersion: GradleVersion,
    ): List<String> {
        val arguments = mutableListOf<String>()
        when (logLevel) {
            LogLevel.DEBUG -> arguments.add("--debug")
            LogLevel.INFO -> arguments.add("--info")
            LogLevel.WARN -> arguments.add("--warn")
            LogLevel.QUIET -> arguments.add("--quiet")
            else -> Unit
        }
        arguments.add("-Pkotlin_version=$kotlinVersion")

        val configurationCacheFlag = configurationCache.toBooleanFlag(gradleVersion)
        if (configurationCacheFlag != null) {
            arguments.add("-Dorg.gradle.unsafe.configuration-cache=$configurationCacheFlag")
            arguments.add("-Dorg.gradle.unsafe.configuration-cache-problems=${configurationCacheProblems.name.lowercase()}")
            arguments.add("-Dorg.gradle.configuration-cache.parallel=true")
        }

        // If isolated projects _explicitly_ enabled, but the configuration cache is disabled, emit the error
        if (isolatedProjects == IsolatedProjectsMode.ENABLED && configurationCacheFlag != true) {
            throw IllegalArgumentException("Isolated projects can't be enabled, if the configuration cache is disabled!")
        }
        // Isolated projects can't be enabled, if the configuration cache is disabled
        val isolatedProjectsFlag = isolatedProjects.toBooleanFlag(gradleVersion) && configurationCacheFlag == true
        arguments.add("-Dorg.gradle.unsafe.isolated-projects=$isolatedProjectsFlag")

        if (parallel) {
            arguments.add("--parallel")
            arguments.add("--max-workers=$maxWorkers")
        } else {
            arguments.add("--no-parallel")
        }

        if (fileSystemWatchEnabled) {
            arguments.add("--watch-fs")
        } else {
            arguments.add("--no-watch-fs")
        }

        arguments.add(if (buildCacheEnabled) "--build-cache" else "--no-build-cache")

        if (stacktraceMode != null) {
            arguments.add("--$stacktraceMode")
        }

        if (compilerArgumentsLogLevel != null) {
            arguments.add("-Pkotlin.internal.compiler.arguments.log.level=$compilerArgumentsLogLevel")
        }

        return arguments.toList()
    }

    enum class ConfigurationCacheProblems {
        FAIL, WARN
    }
}

fun BuildOptions.enableIsolatedProjects() = copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.ENABLED)
fun BuildOptions.disableIsolatedProjects() = copy(isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED)
