package org.jetbrains.kotlin.gradle.declarative.projecttypes.webapplication

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.PackagingExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.WebEcosystemDefinition

@Suppress("UnstableApiUsage")
public interface WebApplicationProjectType : Definition<WebApplicationBuildModel>, WebEcosystemDefinition {

    @get:Nested
    public val dependencies: WebApplicationDependenciesExtension

    @get:Nested
    public val packaging: PackagingExtension
}