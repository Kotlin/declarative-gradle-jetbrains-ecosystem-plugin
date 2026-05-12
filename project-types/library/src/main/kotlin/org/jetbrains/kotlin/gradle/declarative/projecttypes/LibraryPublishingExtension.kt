package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryPublishingExtension : Definition<BuildModel.None> {
    public val group: Property<String>
    public val version: Property<String>
 }