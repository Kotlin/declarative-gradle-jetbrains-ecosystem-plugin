package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JavaJvmCompilationExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.KotlinCompilationExtension

@Suppress("UnstableApiUsage")
public interface LibraryProjectType : Definition<LibraryBuildModel> {
    // Change to LibraryPlatforms type once https://github.com/gradle/gradle/issues/34114 is fixed
    /**
     * See available platforms at [LibraryPlatforms].
     */
    public val platforms: ListProperty<String>

    @get:Nested
    public val jvmPlatform: LibraryJvmEcosystemDefinition

    @get:Nested
    public val java: JavaJvmCompilationExtension

    @get:Nested
    public val kotlin: KotlinCompilationExtension

    @get:Nested
    public val dependencies: LibraryDependenciesExtension
}

public enum class LibraryPlatforms {
    jvm, common, web, ios,
}