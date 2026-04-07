@file:Suppress("UnstableApiUsage")

package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

/**
 * Describes how the application should be packaged.
 */
public interface PackagingExtension : Definition<BuildModel.None>
