package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingBuildModel
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension

public interface LibraryTestingExtension : TestingExtension<LibraryTestingBuildModel> {

    @get:Nested
    public val dependencies: LibraryTestingDependenciesExtension

    @get:Nested
    public val jvmPlatform: LibraryTestingJvmEcosystemDefinition

    @get:Nested
    public val webPlatform: LibraryTestingWebEcosystemDefinition

    @get:Nested
    public val iosPlatform: LibraryTestingIosEcosystemDefinition
}

public interface LibraryTestingBuildModel : TestingBuildModel

@Suppress("UnstableApiUsage")
public interface LibraryTestingJvmEcosystemDefinition : Definition<BuildModel.None> {
    public val useJUnitPlatform: Property<Boolean>
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingWebEcosystemDefinition : Definition<BuildModel.None> {
    public val skip: Property<Boolean>
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingIosEcosystemDefinition : Definition<BuildModel.None>
