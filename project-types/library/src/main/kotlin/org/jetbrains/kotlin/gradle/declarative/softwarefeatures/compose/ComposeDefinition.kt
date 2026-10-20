package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.compose

import org.gradle.api.provider.Property
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface ComposeDefinition : Definition<ComposeBuildModel>

public interface ComposeAndroidDefinition : ComposeDefinition {

    public val compilerExtensionVersion: Property<String>
}