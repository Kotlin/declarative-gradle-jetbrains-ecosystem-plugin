package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JavaJvmCompilationExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JvmEcosystemDefinition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.PackagingExtension

@Suppress("UnstableApiUsage")
public interface JvmApplicationProjectType : Definition<JvmApplicationBuildModel>,
    ApplicationDefinition,
    JvmEcosystemDefinition {

    @get:Nested
    public val java: JavaJvmCompilationExtension

    @get:Nested
    public val dependencies: JvmApplicationDependenciesExtension

    /**
     * Describes how the application should be packaged.
     */
    @get:Nested
    public val packaging: PackagingExtension

    @get:Nested
    public val testing: JvmApplicationTestingExtension
}
