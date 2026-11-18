package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.file.DirectoryProperty
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface XcodeProjectGenerationExtension : Definition<BuildModel.None>, GeneratedXcodeEntryPointModifiers {

    public val path: DirectoryProperty

    public val generatedFormat: GeneratedXcodeProjectFormat

    public val generatedEntryPoint: GeneratedXcodeEntryPoint
}

public enum class GeneratedXcodeProjectFormat {
    auto, pbxproj, xcproj
}

public sealed interface GeneratedXcodeEntryPoint {

    public sealed interface Auto : GeneratedXcodeEntryPoint

    public sealed interface Reference : GeneratedXcodeEntryPoint {
        public val qualifiedReference: String
    }
}