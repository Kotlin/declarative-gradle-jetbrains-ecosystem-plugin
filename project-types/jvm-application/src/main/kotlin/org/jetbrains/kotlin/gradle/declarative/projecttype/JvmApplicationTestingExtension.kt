package org.jetbrains.kotlin.gradle.declarative.projecttype

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension

@Suppress("UnstableApiUsage")
public interface JvmApplicationTestingExtension : TestingExtension<BuildModel.None> {

    public val useJUnitPlatform: Property<Boolean>

    @get:Nested
    public val dependencies: JvmApplicationTestingDependenciesExtension
}
