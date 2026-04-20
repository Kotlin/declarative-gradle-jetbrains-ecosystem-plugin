package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JavaJvmCompilationType

@Suppress("UnstableApiUsage")
public interface JavaJvmCompilationExtension : Definition<JavaJvmCompilationType> {
    @get:Nested
    public val compilerOptions: JavaCompileOptions
}

public interface JavaCompileOptions {
    public val compilerArgs: ListProperty<String>
}