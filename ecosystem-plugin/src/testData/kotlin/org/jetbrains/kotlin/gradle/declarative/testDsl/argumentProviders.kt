package org.jetbrains.kotlin.gradle.declarative.testDsl

import org.gradle.util.GradleVersion
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream
import kotlin.streams.asStream


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GradleTestVersions(
    val minVersion: String = TestVersions.Gradle.MIN_SUPPORTED,
    val maxVersion: String = TestVersions.Gradle.MAX_SUPPORTED,
    val additionalVersions: Array<String> = [],
)

/**
 * Adds another dimension of arguments alongside Gradle Version
 * Example:
 *
 * ```
 * class SomeGradleTest {
 *   @GradleTest
 *   @GradleTestExtraStringArguments("a", "b")
 *   fun testMethod(gradleVersion: GradleVersion, extra: String) {
 *      // JUnit will invoke testMethod as follows
 *      // testMethod(GradleVersion.MIN, "a")
 *      // testMethod(GradleVersion.MIN, "b")
 *      // testMethod(GradleVersion.MAX, "a")
 *      // testMethod(GradleVersion.MAX, "b")
 *   }
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GradleTestExtraStringArguments(
    vararg val values: String
)

/**
 * Parameterized test against different Gradle versions.
 * Test should accept [GradleVersion] as a parameter.
 *
 * By default, [TestVersions.Gradle.MIN_SUPPORTED] and [TestVersions.Gradle.MAX_SUPPORTED] Gradle versions are provided.
 * To modify it use additional [GradleTestVersions] annotation on the test method.
 *
 * @see [GradleTestVersions]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@GradleTestVersions
@ParameterizedTest(name = "{0}: {displayName}")
@ArgumentsSource(GradleArgumentsProvider::class)
annotation class GradleTest

inline fun <reified T : Annotation> findAnnotationOrNull(context: ExtensionContext): T? {
    var nextSuperclass: Class<*>? = context.testClass.get().superclass
    val superClassSequence = if (nextSuperclass != null) {
        generateSequence {
            val currentSuperclass = nextSuperclass
            nextSuperclass = nextSuperclass?.superclass
            currentSuperclass
        }
    } else {
        emptySequence()
    }

    return sequenceOf(
        context.testMethod.orElse(null),
        context.testClass.orElse(null)
    )
        .filterNotNull()
        .plus(superClassSequence)
        .mapNotNull { declaration ->
            declaration.annotations.firstOrNull { it is T }
        }
        .firstOrNull() as T?
        ?: context.testMethod.get().annotations
            .mapNotNull { annotation ->
                annotation.annotationClass.annotations.firstOrNull { it is T }
            }
            .firstOrNull() as T?
}

open class GradleArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(
        context: ExtensionContext,
    ): Stream<out Arguments> {
        val gradleVersions = gradleVersions(context)
        val versionFilter = context.getConfigurationParameter("gradle.integration.tests.gradle.version.filter")
            .map { GradleVersion.version(it) }

        val extraArguments = extraArguments(context) ?: emptyArray()

        return gradleVersions
            .asSequence()
            .filter { gradleVersion -> versionFilter.map { gradleVersion == it }.orElse(true) }
            .flatMap { gradleVersion ->
                if (extraArguments.isNotEmpty()) {
                    extraArguments.asSequence().map { extraArgument -> Arguments.of(gradleVersion, extraArgument) }
                } else sequenceOf(Arguments.of(gradleVersion))
            }
            .asStream()
    }

    protected fun gradleVersions(context: ExtensionContext): Set<GradleVersion> {
        val versionsAnnotation = findAnnotationOrNull<GradleTestVersions>(context) ?: GradleTestVersions()

        fun max(a: GradleVersion, b: GradleVersion) = if (a >= b) a else b
        val minGradleVersion = GradleVersion.version(versionsAnnotation.minVersion)
        // Max is used for cases when test is annotated with `@GradleTestVersions(minVersion = LATEST)` but MAX_SUPPORTED isn't latest
        val maxGradleVersion = max(GradleVersion.version(versionsAnnotation.maxVersion), minGradleVersion)

        val additionalGradleVersions = versionsAnnotation
            .additionalVersions
            .map(GradleVersion::version)
        additionalGradleVersions.forEach {
            assert(it in minGradleVersion..maxGradleVersion) {
                "Additional Gradle version ${it.version} should be between ${minGradleVersion.version} and ${maxGradleVersion.version}"
            }
        }

        return setOf(minGradleVersion, *additionalGradleVersions.toTypedArray(), maxGradleVersion)
    }

    protected fun extraArguments(context: ExtensionContext): Array<out String>? {
        val extraArgumentsAnnotation = findAnnotationOrNull<GradleTestExtraStringArguments>(context)
        return extraArgumentsAnnotation?.values
    }
}
