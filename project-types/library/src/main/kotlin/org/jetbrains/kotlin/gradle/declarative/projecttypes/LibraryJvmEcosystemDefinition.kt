package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JvmEcosystemDefinition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.KotlinJvmCompilationExtension

@Suppress("UnstableApiUsage")
public interface LibraryJvmEcosystemDefinition : Definition<BuildModel.None>, JvmEcosystemDefinition {

    @get:Nested
    public val kotlin: KotlinJvmCompilationExtension
}