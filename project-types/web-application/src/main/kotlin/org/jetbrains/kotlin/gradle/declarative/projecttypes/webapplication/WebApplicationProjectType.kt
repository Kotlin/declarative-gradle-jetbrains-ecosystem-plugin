package org.jetbrains.kotlin.gradle.declarative.projecttypes.webapplication

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface WebApplicationProjectType : Definition<WebApplicationBuildModel> {

    @get:Nested
    public val dependencies: WebApplicationDependenciesExtension
}