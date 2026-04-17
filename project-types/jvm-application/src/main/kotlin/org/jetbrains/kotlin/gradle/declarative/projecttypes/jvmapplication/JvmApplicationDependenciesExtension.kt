package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface JvmApplicationDependenciesExtension : Definition<BuildModel.None>,
    Dependencies, // Unsafe definition
    PlatformDependencyModifiers { // Unsafe definition
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
}
