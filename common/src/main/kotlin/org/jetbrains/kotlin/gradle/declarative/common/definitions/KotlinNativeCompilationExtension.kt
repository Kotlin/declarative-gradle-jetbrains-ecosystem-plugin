package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeCompilerOptions

@Suppress("UnstableApiUsage")
public interface KotlinNativeCompilationExtension : Definition<BuildModel.None> {
    @get:Nested
    public val compilerOptions: KotlinNativeCompilerOptions
}