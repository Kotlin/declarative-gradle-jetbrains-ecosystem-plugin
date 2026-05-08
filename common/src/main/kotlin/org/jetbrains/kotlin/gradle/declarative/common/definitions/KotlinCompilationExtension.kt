package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions


@Suppress("UnstableApiUsage")
public interface KotlinCompilationExtension : Definition<BuildModel.None> {
    @get:Nested
    public val compilerOptions: KotlinCommonCompilerOptions
}
