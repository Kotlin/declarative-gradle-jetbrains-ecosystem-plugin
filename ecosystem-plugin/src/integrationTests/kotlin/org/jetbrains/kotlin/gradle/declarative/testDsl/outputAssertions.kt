package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import java.util.*
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

