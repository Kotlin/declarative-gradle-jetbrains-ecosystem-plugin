package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryDependenciesExtension : Definition<BuildModel.None>, Dependencies {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
}
