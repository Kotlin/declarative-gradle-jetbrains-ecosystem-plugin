package org.jetbrains.kotlin.gradle.declarative.softwarefeature.resource

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface ResourceDependencyDefinition : Definition<BuildModel.None>, Dependencies {
    public val resource: DependencyCollector
}