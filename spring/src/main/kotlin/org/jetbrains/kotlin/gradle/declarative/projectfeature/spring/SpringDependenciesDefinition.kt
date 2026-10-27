package org.jetbrains.kotlin.gradle.declarative.projectfeature.spring

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface SpringDependenciesDefinition : Definition<BuildModel.None>, Dependencies {
    public val developmentOnly: DependencyCollector
    public val resource: DependencyCollector
}
