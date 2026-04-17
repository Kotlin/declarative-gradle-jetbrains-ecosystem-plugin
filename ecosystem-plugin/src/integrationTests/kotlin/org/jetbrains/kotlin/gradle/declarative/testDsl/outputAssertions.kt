package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.api.logging.LogLevel
import org.gradle.testkit.runner.BuildResult
import org.intellij.lang.annotations.Language
import kotlin.test.assertEquals

/**
 * Asserts Gradle output contains [expectedSubString] string.
 */
fun BuildResult.assertOutputContains(
    expectedSubString: String,
    message: String = "Build output does not contain \"$expectedSubString\"",
) {
    assert(output.contains(expectedSubString)) {
        printBuildOutput()
        message
    }
}

/**
 * Asserts Gradle output contains any of [expectedSubStrings] strings.
 */
fun BuildResult.assertOutputContainsAny(
    vararg expectedSubStrings: String,
) {
    assert(expectedSubStrings.any { output.contains(it) }) {
        printBuildOutput()
        "Build output does not contain any of \"${expectedSubStrings.toList()}\""
    }
}

/**
 * Asserts Gradle output does not contain [notExpectedSubString] string.
 *
 * @param wrappingCharsCount amount of chars to include before and after [notExpectedSubString] occurrence
 */
fun BuildResult.assertOutputDoesNotContain(
    notExpectedSubString: String,
    wrappingCharsCount: Int = 100,
) {
    assert(!output.contains(notExpectedSubString)) {
        printBuildOutput()

        // In case if notExpectedSubString is multiline string
        val occurrences = mutableListOf<Pair<Int, Int>>()
        var startIndex = output.indexOf(notExpectedSubString)
        var endIndex = startIndex + notExpectedSubString.length
        do {
            occurrences.add(startIndex to endIndex)
            startIndex = output.indexOf(notExpectedSubString, endIndex)
            endIndex = startIndex + notExpectedSubString.length
        } while (startIndex != -1)

        val linesContainingSubString = occurrences.map { (startIndex, endIndex) ->
            output.subSequence(
                (startIndex - wrappingCharsCount).coerceAtLeast(0),
                (endIndex + wrappingCharsCount).coerceAtMost(output.length)
            )
        }

        """
        |
        |===> Build output contains non-expected sub-string:
        |'$notExpectedSubString'
        |===> in following places:
        |${linesContainingSubString.joinToString(separator = "\n|===> Next case:\n")}
        |===> End of occurrences
        |
        """.trimMargin()
    }
}

/**
 * Assert build output contains one or more strings matching [expected] regex.
 */
fun BuildResult.assertOutputContains(
    expected: Regex,
    message: String = "Build output does not contain any line matching '$expected' regex.",
) {
    assert(output.contains(expected)) {
        printBuildOutput()

        message
    }
}

/**
 * Asserts build output does not contain any lines matching [regexToCheck] regex.
 */
fun BuildResult.assertOutputDoesNotContain(
    regexToCheck: Regex,
) {
    assert(!output.contains(regexToCheck)) {
        printBuildOutput()

        val matchedStrings = regexToCheck
            .findAll(output)
            .map { it.value }
            .joinToString(prefix = "  ", separator = "\n  ")
        "Build output contains following regex '$regexToCheck' matches:\n$matchedStrings"
    }
}

/**
 * Asserts build output contains exactly [expectedCount] of occurrences of [expected] string.
 */
fun BuildResult.assertOutputContainsExactlyTimes(
    expected: String,
    expectedCount: Int = 1,
) {
    assertOutputContainsExactlyTimes(expected.toRegex(RegexOption.LITERAL), expectedCount)
}

fun BuildResult.assertOutputContainsExactlyTimes(
    expected: Regex,
    expectedCount: Int = 1,
) {
    val occurrenceCount = expected.findAll(output).count()
    if (occurrenceCount != expectedCount) {
        printBuildOutput()
        assertEquals(expectedCount, occurrenceCount, "Build output contains unexpected number of '$expected' string occurrences.")
    }
}

/**
 * This function searches for a given parameter in a multi-line output string and returns its value.
 *
 * The output string is assumed to be in the form of key-value pairs separated by an equal sign (‘=’) on each line.
 *
 * If the specified parameter name is found at the end of a key, the corresponding value is returned.
 * If the parameter is not found, the function returns null.
 */
fun findParameterInOutput(name: String, output: String): String? =
    output.lineSequence().mapNotNull { line ->
        val (key, value) = line.split('=', limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
        if (key.endsWith(name)) value else null
    }.firstOrNull()

fun BuildResult.assertCompilerArgument(
    taskPath: String,
    expectedArgument: String,
    logLevel: LogLevel = LogLevel.DEBUG
) {
    val compilerArguments = extractTaskCompilerArguments(taskPath, logLevel)

    assert(
        compilerArguments.contains(expectedArgument) || (expectedArgument.contains("=") && compilerArguments.contains(
            expectedArgument.replaceFirst("=", " ")
        ))
    ) {
        printBuildOutput()

        "$taskPath task compiler arguments don't contain $expectedArgument. Actual content: $compilerArguments"
    }
}

/**
 * Extracts compiler arguments used in compilation for a given Kotlin task under [taskPath] path.
 *
 * @param logLevel [LogLevel] with which build was running, default to [LogLevel.INFO].
 */
fun BuildResult.extractTaskCompilerArguments(
    taskPath: String,
    logLevel: LogLevel = LogLevel.INFO
): String {
    val taskOutput = getOutputForTask(taskPath, logLevel)
    return taskOutput.lines().first {
        it.contains("Kotlin compiler args:")
    }.substringAfter("Kotlin compiler args:")
}

/**
 * Gets the output produced by a specific task during a Gradle build.
 *
 * @param taskPath The path of the task whose output should be retrieved.
 * @param logLevel The given output contains no more than the [logLevel] logs.
 *
 * @return The output produced by the specified task during the build.
 *
 * @throws IllegalStateException if the specified task path does not match any tasks in the build.
 */
fun BuildResult.getOutputForTask(taskPath: String, logLevel: LogLevel = LogLevel.DEBUG): String =
    getOutputForTask(taskPath, output, logLevel)

/**
 * Gets the output produced by a specific task during a Gradle build.
 *
 * @param taskPath The path of the task whose output should be retrieved.
 * @param output The output from which we should extract task's output
 * @param logLevel The given output contains no more than the [logLevel] logs.
 *
 * @return The output produced by the specified task during the build.
 *
 * @throws IllegalStateException if the specified task path does not match any tasks in the build.
 */
fun getOutputForTask(taskPath: String, output: String, logLevel: LogLevel = LogLevel.DEBUG): String = (
        when (logLevel) {
            LogLevel.INFO -> taskOutputRegexForInfoLog(taskPath)
            LogLevel.DEBUG -> taskOutputRegexForDebugLog(taskPath)
            else -> throw IllegalStateException("Unsupported log lever for task output was given: $logLevel")
        })
    .findAll(output)
    .map {
        when (logLevel) {
            LogLevel.INFO -> it.groupValues[2] // `( FAILED)` defines a group with index 1
            else -> it.groupValues[1]
        }
    }
    .joinToString(System.lineSeparator())
    .ifEmpty {
        error(
            """
            Could not find output for task $taskPath.
            =================
            Build output is:
            $output 
            =================     
            """.trimIndent()
        )
    }

@Language("RegExp")
private fun taskOutputRegexForDebugLog(
    taskName: String,
) = """
    \[org\.gradle\.internal\.operations\.DefaultBuildOperationRunner] Build operation 'Task $taskName' started
    ([\s\S]+?)
    \[org\.gradle\.internal\.operations\.DefaultBuildOperationRunner] Build operation 'Task $taskName' completed
    """.trimIndent()
    .replace("\n", "")
    .toRegex()

@Language("RegExp")
private fun taskOutputRegexForInfoLog(
    taskName: String,
) =
    """
    ^\s*$\r?
    ^> Task $taskName( FAILED)?$\r?
    ([\s\S]+?)\r?
    ^\s*$\r?
    """.trimIndent()
        .toRegex(RegexOption.MULTILINE)


