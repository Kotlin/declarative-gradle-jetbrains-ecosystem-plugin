package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.JavaJvmCompilationExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinCompilationExtension

@Suppress("UnstableApiUsage")
public interface LibraryProjectType : Definition<LibraryBuildModel> {
    // Change to LibraryPlatforms type once https://github.com/gradle/gradle/issues/34114 is fixed
    /**
     * See available platforms at [LibraryPlatforms].
     */
    public val platforms: ListProperty<String>

    //TODO: temporary solution until we have the compose software feature
    public val enableCompose: Property<Boolean>

    @get:Nested
    public val androidPlatform: LibraryAndroidEcosystemDefinition

    @get:Nested
    public val jvmPlatform: LibraryJvmEcosystemDefinition

    @get:Nested
    public val webPlatform: LibraryWebEcosystemDefinition

    @get:Nested
    public val iosPlatform: LibraryIosEcosystemDefinition

    @get:Nested
    public val java: JavaJvmCompilationExtension

    @get:Nested
    public val kotlin: KotlinCompilationExtension

    @get:Nested
    public val dependencies: LibraryDependenciesExtension

    @get:Nested
    public val publishing: LibraryPublishingExtension

    @get:Nested
    public val testing: LibraryTestingExtension
}

public enum class LibraryPlatforms {
    jvm, common, web, ios, android
}