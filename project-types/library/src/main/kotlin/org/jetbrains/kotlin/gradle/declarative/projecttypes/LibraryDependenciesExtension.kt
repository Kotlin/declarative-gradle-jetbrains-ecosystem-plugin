package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.api.plugins.jvm.TestFixturesDependencyModifiers
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryDependenciesExtension : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val api: DependencyCollector
    public val implementation: DependencyCollector

    @get:Nested
    public val jvmPlatform: LibraryJvmPlatformDependencies

    @get:Nested
    public val webPlatform: LibraryWebPlatformDependencies

    @get:Nested
    public val iosPlatformDependencies: LibraryIosPlatformDependencies
}

@Suppress("UnstableApiUsage")
public interface LibraryJvmPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryWebPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryIosPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingDependenciesExtension : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val implementation: DependencyCollector

    @get:Nested
    public val jvmPlatform: LibraryTestingJvmPlatformDependencies

    @get:Nested
    public val webPlatform: LibraryTestingWebPlatformDependencies

    @get:Nested
    public val iosPlatformDependencies: LibraryTestingIosPlatformDependencies
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingJvmPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers,
    TestFixturesDependencyModifiers {
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingWebPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val implementation: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingIosPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val implementation: DependencyCollector
}
