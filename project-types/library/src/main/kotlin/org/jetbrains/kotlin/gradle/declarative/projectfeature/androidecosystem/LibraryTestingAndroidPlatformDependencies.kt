package org.jetbrains.kotlin.gradle.declarative.projectfeature.androidecosystem

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryAndroidPlatformTestDependencies : Dependencies, PlatformDependencyModifiers {
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingAndroidPlatformDependencies : Definition<BuildModel.None> {

    @get:Nested
    public val hostTest: LibraryAndroidPlatformTestDependencies

    @get:Nested
    public val deviceTest: LibraryAndroidPlatformTestDependencies
}