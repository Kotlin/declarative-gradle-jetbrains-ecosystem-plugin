package org.jetbrains.kotlin.gradle.declarative.common.sync

import org.jetbrains.kotlin.gradle.dsl.KotlinNativeCompilerOptions

/**
 * Syncs [from] instance of [KotlinNativeCompilerOptions] into [into] as convention,
 * using [fallback] instance values via `.orElse` method when [from] values are not set.
 */
internal fun syncKotlinNativeCompilerOptionsAsConvention(
    from: KotlinNativeCompilerOptions,
    into: KotlinNativeCompilerOptions,
    fallback: KotlinNativeCompilerOptions,
) {
    // KotlinCommonCompilerToolOptions
    into.allWarningsAsErrors.convention(from.allWarningsAsErrors.orElse(fallback.allWarningsAsErrors))
    into.extraWarnings.convention(from.extraWarnings.orElse(fallback.extraWarnings))
    into.suppressWarnings.convention(from.suppressWarnings.orElse(fallback.suppressWarnings))
    into.verbose.convention(from.verbose.orElse(fallback.verbose))
    into.freeCompilerArgs.convention(from.freeCompilerArgs.orElse(fallback.freeCompilerArgs))

    // KotlinCommonCompilerOptions
    into.apiVersion.convention(from.apiVersion.orElse(fallback.apiVersion))
    into.languageVersion.convention(from.languageVersion.orElse(fallback.languageVersion))
    into.optIn.convention(from.optIn.orElse(fallback.optIn))
    into.progressiveMode.convention(from.progressiveMode.orElse(fallback.progressiveMode))

    // KotlinNativeCompilerOptions
    // NOTE: For `moduleName`, KotlinNativeTarget's convention (`into`) gets overridden by the provider constructed
    // using `from` and `fallback` which potentially realizes to `null`. In order to not force users to manually declare
    // the `moduleName` every time they declare a native platform in a library project, we use the already-set convention
    // in KotlinNativeTarget as the fallback value for the constructed provider.
    into.moduleName.convention(from.moduleName.orElse(fallback.moduleName).orElse(into.moduleName.get()))
}