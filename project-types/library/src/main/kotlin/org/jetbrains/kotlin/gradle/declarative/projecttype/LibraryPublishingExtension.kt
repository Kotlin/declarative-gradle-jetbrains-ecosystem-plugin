package org.jetbrains.kotlin.gradle.declarative.projecttype

import org.gradle.api.provider.Property
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryPublishingExtension : Definition<LibraryBuildModel> {
    public val group: Property<String>
    public val version: Property<String>
 }