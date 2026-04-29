package org.jetbrains.kotlin.gradle.declarative.projecttypes.webapplication

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface WebApplicationDependenciesExtension : Definition<BuildModel.None>,
    Dependencies, // Unsafe definition
    PlatformDependencyModifiers { // Unsafe definition
    public val implementation: DependencyCollector
}
