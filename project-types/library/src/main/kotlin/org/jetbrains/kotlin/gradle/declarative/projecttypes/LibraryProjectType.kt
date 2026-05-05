package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.provider.ListProperty
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryProjectType : Definition<LibraryBuildModel> {
    // Change to LibraryPlatforms type once https://github.com/gradle/gradle/issues/34114 is fixed
    /**
     * See available platforms at [LibraryPlatforms].
     */
    public val platforms: ListProperty<String>
}

public enum class LibraryPlatforms {
    jvm, common, web, ios,
}