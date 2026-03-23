package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.util.GradleVersion
import java.io.PrintWriter
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.*

/**
 * Makes a snapshot of the current state of [TestProject] into [destinationPath].
 *
 * Method copies all files into `destinationPath/testProjectName/GradleVersion` directory
 * and setup buildable project.
 *
 * To run task with the same build option as test - use `run.sh` (or `run.bat`) script.
 */
@OptIn(ExperimentalPathApi::class)
fun TestProject.makeSnapshotTo(
    destinationPath: String,
    buildOptions: BuildOptions = this.buildOptions,
) {
    val dest = Path(destinationPath)
        .resolve(projectName)
        .resolve(gradleVersion.version)
        .also {
            if (it.exists()) it.deleteRecursively()
            it.createDirectories()
        }

    projectPath.copyRecursively(dest)

    val gradlePropertiesFromBuildOptions = buildOptions.asGradleProperties(gradleVersion)
    val gradlePropertiesContent = gradlePropertiesFromBuildOptions.entries.joinToString("\n") { (k, v) -> "${k}=${v}" }

    dest.walk()
        .filter { it.isRegularFile() }
        .filter { it.name == "settings.gradle" || it.name == "settings.gradle.kts" }
        .map { it.resolveSibling("gradle.properties") }
        .forEach { gradlePropertiesFile ->
            if (gradlePropertiesFile.exists()) {
                val propertiesRegex = """^\s*(\S+)=(.*)""".toRegex()
                val content = gradlePropertiesFile.readLines()
                    .map { it.trim() }
                    .map { line ->
                        val match = propertiesRegex.matchEntire(line) ?: return@map line
                        val (key, value) = match.destructured
                        val overriddenValue = gradlePropertiesFromBuildOptions[key]
                        if (overriddenValue != null && value != overriddenValue) {
                            "# $line // overridden by buildOptions with\n$key=$overriddenValue\n"
                        } else {
                            "${line}\n"
                        }
                    }
                gradlePropertiesFile.writeLines(content)
            }

            gradlePropertiesFile.appendText("# Gradle Properties from project's buildOptions\n$gradlePropertiesContent\n")
        }

    dest.resolve("run.sh").run {
        writeText(
            """
            |#!/usr/bin/env sh
            |${formatEnvironmentForScript(envCommand = "export")}
            |./gradlew ${buildOptions.toArguments(gradleVersion).joinToString(separator = " ")} ${'$'}@ 
            |""".trimMargin()
        )

        if ("Windows" !in System.getProperty("os.name")) {
            setPosixFilePermissions(
                setOf(
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                )
            )
        }
    }

    dest.resolve("run.bat").run {
        writeText(
            """
            |@rem Executing Gradle build
            |${formatEnvironmentForScript(envCommand = "set")}
            |gradlew.bat ${buildOptions.toArguments(gradleVersion).joinToString(separator = " ")} %* 
            |""".trimMargin()
        )
    }

    val wrapperDir = dest.resolve("gradle/wrapper").createDirectories()
    wrapperDir.resolve("gradle-wrapper.properties").writeText(
        """
        distributionUrl=https\://services.gradle.org/distributions/gradle-${gradleVersion.version}-bin.zip
        """.trimIndent()
    )
    // Copied from 'Wrapper' task class implementation
    val projectRoot = Path("../")
    projectRoot.resolve("gradle/wrapper/gradle-wrapper.jar").run {
        copyTo(wrapperDir.resolve(fileName))
    }
    projectRoot.resolve("gradlew").run {
        copyTo(dest.resolve(fileName))
    }
    projectRoot.resolve("gradlew.bat").run {
        copyTo(dest.resolve(fileName))
    }
}

private fun BuildOptions.asGradleProperties(gradleVersion: GradleVersion): Map<String, String> {
    val propertyRegex = """^-[DP](.+)=(.*)""".toRegex()
    return toArguments(gradleVersion)
        .mapNotNull {
            val match = propertyRegex.matchEntire(it) ?: return@mapNotNull null
            match.groupValues[1] to match.groupValues[2]
        }.toMap()
}

private fun TestProject.formatEnvironmentForScript(envCommand: String): String {
    return environmentVariables.environmentalVariables.asSequence().joinToString(separator = "\n|") { (key, value) ->
        "$envCommand $key=\"$value\""
    }
}

/**
 * Adds the given options to a Gradle property specified by name, in the project's Gradle properties file.
 * If the property does not exist, it is created.
 * @param propertyName The name of the Gradle property to modify or create.
 * @param propertyValues Map with key = "option prefix", and value = "option value".
 *                       For example, for options: -Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError
 *                       Map would be look like: Map.of("-Xmx", "-Xmx2g",
 *                                                      "-XX:MaxMetaspaceSize","-XX:MaxMetaspaceSize=512m",
 *                                                      "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+HeapDumpOnOutOfMemoryError" )
 */
fun GradleProject.addPropertyToGradleProperties(
    propertyName: String,
    propertyValues: Map<String, String>,
) {
    if (!gradleProperties.exists()) gradleProperties.createFile()

    val propertiesContent = gradleProperties.readText()
    val (existingPropertyLine, otherLines) = propertiesContent
        .lines()
        .partition {
            it.trim().startsWith(propertyName)
        }

    if (existingPropertyLine.isEmpty()) {
        gradleProperties.writeText(
            """
            |${propertyName}=${propertyValues.values.joinToString(" ")}
            | 
            |$propertiesContent
            """.trimMargin()
        )
    } else {
        val argsLine = existingPropertyLine.single()
        val optionsToRewrite = mutableListOf<String>()
        val appendedOptions = buildString {
            propertyValues.forEach {
                if (argsLine.contains(it.key) &&
                    !argsLine.contains(it.value)
                ) optionsToRewrite.add(it.value)
                else
                    if (!argsLine.contains(it.key)) append(" ${it.value}")
            }
        }

        assert(optionsToRewrite.isEmpty()) {
            """
            |You are trying to write options: $optionsToRewrite 
            |for property: $propertyName 
            |in $gradleProperties
            |But these options are already exists with another values.
            |Current property value is: $argsLine
            """.trimMargin()
        }

        gradleProperties.writeText(
            """
            |$argsLine$appendedOptions
            |
            |${otherLines.joinToString(separator = "\n")}
            """.trimMargin()
        )

    }
}

val Throwable.fullMessage
    get(): String = java.io.StringWriter().use {
        PrintWriter(it).use {
            this.printStackTrace(it)
        }
        it
    }.toString()

/**
 * Returns a list of subprojects for the [TestProject], explicitly specified by their names.
 * This method can be used as an explicit alternative to `subprojects { ... }`.
 * If a subproject name is an empty string (`""`) or a single dot (`"."`), it will be replaced by the [TestProject] itself.
 *
 * @return A list of subprojects corresponding to the specified names, with special handling for empty or dot names.
 */
internal fun TestProject.subprojects(firstName: String, vararg names: String): Iterable<GradleProject> {
    return arrayOf(firstName, *names).map { name ->
        when (name) {
            "", "." -> this
            else -> subProject(name)
        }
    }
}
