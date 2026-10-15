package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.AndroidEcosystemDefinition

@Suppress("UnstableApiUsage")
public interface LibraryAndroidEcosystemDefinition : Definition<BuildModel.None>, AndroidEcosystemDefinition