package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.provider.ListProperty
import org.gradle.features.binding.BuildModel


@Suppress("UnstableApiUsage")
public interface LibraryBuildModel : BuildModel {
    public val enabledPlatforms: ListProperty<LibraryPlatforms>
}