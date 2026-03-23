package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.testkit.runner.BuildResult

internal fun BuildResult.printBuildOutput() {
    println(failedAssertionOutput())
}
internal fun BuildResult.failedAssertionOutput() = """
        |Failed assertion build output:
        |#######################
        |$output
        |#######################
        |
        """.trimMargin()

internal fun String.normalizeLineEndings(): String = replace("\n", System.lineSeparator())
