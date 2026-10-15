package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryIosFrameworkPublishingExtension : Definition<BuildModel.None>, Dependencies, PlatformDependencyModifiers {

    public val static: Property<Boolean>

    public val linkerOpts: ListProperty<String>

    public val export: DependencyCollector

    public val transitiveExport: Property<Boolean>
}