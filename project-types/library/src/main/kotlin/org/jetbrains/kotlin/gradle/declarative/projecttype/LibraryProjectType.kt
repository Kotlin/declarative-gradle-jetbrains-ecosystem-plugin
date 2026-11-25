package org.jetbrains.kotlin.gradle.declarative.projecttype

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.JavaJvmCompilationExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinCompilationExtension

public interface LibraryProjectType : LibraryDefinition {
    /**
     * See available platforms at [LibraryPlatform].
     */
    public val platforms: ListProperty<LibraryPlatform>

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

public enum class LibraryPlatform {
    jvm, common, web, ios, android
}