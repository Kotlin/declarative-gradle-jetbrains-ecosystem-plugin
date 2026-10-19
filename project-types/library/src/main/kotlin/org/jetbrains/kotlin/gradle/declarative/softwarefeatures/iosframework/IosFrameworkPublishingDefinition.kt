package org.jetbrains.kotlin.gradle.declarative.softwarefeatures

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface IosFrameworkPublishingDefinition : Definition<BuildModel.None>, Dependencies {

    public val static: Property<Boolean>

    public val linkerOpts: ListProperty<String>

    public val export: DependencyCollector

    public val transitiveExport: Property<Boolean>
}