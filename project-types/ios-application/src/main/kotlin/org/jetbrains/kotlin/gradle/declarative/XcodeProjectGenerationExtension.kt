package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface XcodeProjectGenerationExtension : Definition<BuildModel.None>, IosApplicationEntryPointModifiers {

    public val entryPoint: Property<IosApplicationEntryPoint>
}

public sealed interface IosApplicationEntryPoint {

    public sealed interface Auto : IosApplicationEntryPoint

    public sealed interface Reference : IosApplicationEntryPoint {
        public val qualifiedReference: String
    }
}