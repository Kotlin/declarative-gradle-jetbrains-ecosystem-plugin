package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryDependenciesExtension : Definition<BuildModel.None>, Dependencies {
    public val api: DependencyCollector
    public val implementation: DependencyCollector

    @get:Nested
    public val jvmPlatform: LibraryJvmPlatformDependencies

    @get:Nested
    public val webPlatform: LibraryWebPlatformDependencies
}

@Suppress("UnstableApiUsage")
public interface LibraryJvmPlatformDependencies : Definition<BuildModel.None>, Dependencies {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryWebPlatformDependencies : Definition<BuildModel.None>, Dependencies {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
}
