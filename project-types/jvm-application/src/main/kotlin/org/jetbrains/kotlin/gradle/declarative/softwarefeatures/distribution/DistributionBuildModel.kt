package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.distribution

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.distribution.Distribution
import org.gradle.features.binding.BuildModel

@Suppress("UnstableApiUsage")
public interface DistributionBuildModel : BuildModel {
    public val distributions: NamedDomainObjectContainer<Distribution>
}

internal abstract class DefaultDistributionBuildModel : DistributionBuildModel {
    lateinit var _distributions: NamedDomainObjectContainer<Distribution>

    override val distributions: NamedDomainObjectContainer<Distribution>
        get() = _distributions
}