package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface JvmApplicationProjectType : Definition<JvmApplicationBuildModel> {

    /**
     * Describes how the application should be packaged.
     */
    @get:Nested
    public val packaging: PackagingExtension
}