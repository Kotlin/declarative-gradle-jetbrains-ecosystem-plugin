package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.spring

import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface SpringPackagingDefinition : Definition<SpringPackagingBuildModel> {
    @get:Nested
    public val bootBuildImage: SpringBootBuildImage
}

public interface SpringBootBuildImage {
    public val environment: MapProperty<String, String>
}
