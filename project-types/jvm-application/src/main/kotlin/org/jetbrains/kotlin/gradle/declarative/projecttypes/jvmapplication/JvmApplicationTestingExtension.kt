package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension

public interface JvmApplicationTestingExtension : TestingExtension {

    public val useJUnitPlatform: Property<Boolean>

    @get:Nested
    public val dependencies: JvmApplicationTestingDependenciesExtension
}
