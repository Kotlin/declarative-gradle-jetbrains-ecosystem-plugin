package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.KotlinJvmCompilationType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

@Suppress("UnstableApiUsage")
public interface KotlinJvmCompilationExtension : Definition<KotlinJvmCompilationType> {
    @get:Nested
    public val compilerOptions: KotlinJvmCompilerOptions
}
