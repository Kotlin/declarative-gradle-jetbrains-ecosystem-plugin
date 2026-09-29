package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.JavaJvmCompilationExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinCompilationExtension
import javax.inject.Inject
import kotlin.jvm.java

@Suppress("UnstableApiUsage")
public interface LibraryProjectType : Definition<LibraryBuildModel> {
    /**
     * See available platforms at [LibraryPlatform].
     */
    public val platforms: ListProperty<LibraryPlatform>

    //TODO: temporary solution until we have the compose software feature
    public val enableCompose: Property<Boolean>

    @get:Nested
    public val jvmPlatform: LibraryJvmEcosystemDefinition

    @get:Nested
    public val webPlatform: LibraryWebEcosystemDefinition

    @get:Nested
    public val iosPlatform: LibraryIosEcosystemDefinition

    @get:Nested
    public val androidPlatform: LibraryAndroidEcosystemDefinition

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

internal abstract class DefaultLibraryProjectType @Inject constructor(objectFactory: ObjectFactory) : LibraryProjectType {

    override val androidPlatform: LibraryAndroidEcosystemDefinition = objectFactory.newInstance(DefaultLibraryAndroidEcosystemDefinition::class.java)

    override val testing: LibraryTestingExtension = objectFactory.newInstance(DefaultLibraryTestingExtension::class.java)
}