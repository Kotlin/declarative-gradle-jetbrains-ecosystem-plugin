package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryIosFrameworkPublishingExtension : Definition<BuildModel.None>, Dependencies, PlatformDependencyModifiers {

    public val baseName: Property<String>

    // NOTE: Cannot be named `isStatic` due to Kotlin naming conventions causing class generations issues in Gradle
    // More at https://github.com/gradle/gradle/issues/18543
    public val static: Property<Boolean>

    public val linkerOpts: ListProperty<String>

    public val freeCompilerArgs: ListProperty<String>

    public val binaryOptions: MapProperty<String, String>

    public val export: DependencyCollector

    public val transitiveExport: Property<Boolean>

    public val outputDirectory: DirectoryProperty
}