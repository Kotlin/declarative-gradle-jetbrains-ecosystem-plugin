package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.IosSubplatform
import org.jetbrains.kotlin.gradle.declarative.common.definitions.KotlinNativeCompilationExtension

@Suppress("UnstableApiUsage")
public interface IosApplicationProjectType : Definition<IosApplicationBuildModel> {

    public val subplatforms: ListProperty<IosSubplatform>

    @get:Nested
    public val kotlin: KotlinNativeCompilationExtension

    @get:Nested
    public val xcodeProject: XcodeProjectGenerationExtension

    @get:Nested
    public val dependencies: IosApplicationDependenciesExtension
}