package org.jetbrains.kotlin.gradle.declarative.common.sync

import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompilerOptions

/**
 * Syncs [from] instance of [KotlinJsCompilerOptions] into [into] as convention,
 * using [fallback] instance values via `.orElse` method when [from] values are not set.
 */
internal fun syncKotlinJsCompilerOptionsAsConvention(
    from: KotlinJsCompilerOptions,
    into: KotlinJsCompilerOptions,
    fallback: KotlinJsCompilerOptions,
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

    // KotlinJsCompilerOptions
    into.moduleKind.convention(from.moduleKind.orElse(fallback.moduleKind))
    into.sourceMap.convention(from.sourceMap.orElse(fallback.sourceMap))
    into.sourceMapEmbedSources.convention(from.sourceMapEmbedSources.orElse(fallback.sourceMapEmbedSources))
    into.sourceMapNamesPolicy.convention(from.sourceMapNamesPolicy.orElse(fallback.sourceMapNamesPolicy))
    into.target.convention(from.target.orElse(fallback.target))
}
