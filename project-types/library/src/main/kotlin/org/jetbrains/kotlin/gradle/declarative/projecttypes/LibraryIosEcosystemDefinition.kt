package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.KotlinNativeCompilationExtension

@Suppress("UnstableApiUsage")
public interface LibraryIosEcosystemDefinition : Definition<BuildModel.None> {
    @get:Nested
    public val kotlin: KotlinNativeCompilationExtension
}
