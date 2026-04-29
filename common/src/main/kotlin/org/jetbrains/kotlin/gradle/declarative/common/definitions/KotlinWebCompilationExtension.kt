package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompilerOptions

@Suppress("UnstableApiUsage")
public interface KotlinWebCompilationExtension : Definition<BuildModel.None> {
    @get:Nested
    public val compilerOptions: KotlinJsCompilerOptions
}