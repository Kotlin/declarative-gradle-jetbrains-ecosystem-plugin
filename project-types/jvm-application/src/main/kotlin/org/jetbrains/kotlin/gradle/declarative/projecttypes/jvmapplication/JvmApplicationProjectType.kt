package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JvmEcosystemDefinition

@Suppress("UnstableApiUsage")
public interface JvmApplicationProjectType : Definition<JvmApplicationBuildModel>,
    ApplicationDefinition,
    JvmEcosystemDefinition {

    /**
     * Describes how the application should be packaged.
     */
    @get:Nested
    public val packaging: PackagingExtension
}