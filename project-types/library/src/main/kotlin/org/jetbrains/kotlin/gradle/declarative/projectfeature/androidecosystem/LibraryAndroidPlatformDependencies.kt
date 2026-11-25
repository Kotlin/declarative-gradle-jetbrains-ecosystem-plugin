package org.jetbrains.kotlin.gradle.declarative.projectfeature.androidecosystem

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryAndroidPlatformDependencies : Definition<BuildModel.None>,
    Dependencies,
    PlatformDependencyModifiers {
    public val api: DependencyCollector
    public val implementation: DependencyCollector
    public val compileOnly: DependencyCollector
    public val runtimeOnly: DependencyCollector
    public val runtimeClasspath: DependencyCollector
}