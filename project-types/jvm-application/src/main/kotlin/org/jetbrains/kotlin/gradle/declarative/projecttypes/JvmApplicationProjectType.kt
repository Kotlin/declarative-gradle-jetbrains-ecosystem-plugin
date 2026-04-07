package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.buildmodels.JvmApplicationBuildModel

@Suppress("UnstableApiUsage")
public interface JvmApplicationProjectType : Definition<JvmApplicationBuildModel> {

    /**
     * Describes how the application should be packaged.
     */
    @get:Nested
    public val packaging: PackagingExtension
}
