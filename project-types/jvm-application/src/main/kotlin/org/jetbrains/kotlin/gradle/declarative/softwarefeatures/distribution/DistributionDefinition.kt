package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.distribution

import org.gradle.api.provider.Property
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface DistributionDefinition : Definition<DistributionBuildModel> {
    public val name: Property<String>
    public val classifier: Property<String>
}
