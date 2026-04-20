package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface JavaJvmCompilationExtension : Definition<BuildModel.None> {
    @get:Nested
    public val compilerOptions: JavaCompileOptions
}

public interface JavaCompileOptions {
    public val compilerArgs: ListProperty<String>
}