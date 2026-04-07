package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

/**
 * Describes how the application should be packaged.
 */
@Suppress("UnstableApiUsage")
public interface PackagingExtension : Definition<BuildModel.None>