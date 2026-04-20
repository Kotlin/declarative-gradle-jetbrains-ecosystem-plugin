package org.jetbrains.kotlin.gradle.declarative.common.sync

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

/**
 * Syncs [from] instance of [KotlinJvmCompilerOptions] into [into] as convention,
 * using [fallback] instance values via `.orElse` method when [from] values are not set.
 */
internal fun syncKotlinJvmCompilerOptionsAsConvention(
    from: KotlinJvmCompilerOptions,
    into: KotlinJvmCompilerOptions,
    fallback: KotlinJvmCompilerOptions,
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

    // KotlinJvmCompilerOptions
    into.javaParameters.convention(from.javaParameters.orElse(fallback.javaParameters))
    into.jvmDefault.convention(from.jvmDefault.orElse(fallback.jvmDefault))
    into.jvmTarget.convention(from.jvmTarget.orElse(fallback.jvmTarget))
    into.moduleName.convention(from.moduleName.orElse(fallback.moduleName))
    into.noJdk.convention(from.noJdk.orElse(fallback.noJdk))
}
