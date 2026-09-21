package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface LibraryIosFrameworkPublishingExtension : Definition<BuildModel.None> {

    public val baseName: Property<String>

    // NOTE: Cannot be named `isStatic` due to Kotlin naming conventions causing class generations issues in Gradle
    // More at https://github.com/gradle/gradle/issues/18543
    public val static: Property<Boolean>

    public val linkerOpts: ListProperty<String>

    public val freeCompilerArgs: ListProperty<String>

    public val outputDirectory: DirectoryProperty
}