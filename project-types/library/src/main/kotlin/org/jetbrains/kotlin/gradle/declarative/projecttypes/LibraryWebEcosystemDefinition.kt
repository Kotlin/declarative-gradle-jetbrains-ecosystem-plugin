package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.KotlinWebCompilationExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.WebEcosystemDefinition

@Suppress("UnstableApiUsage")
public interface LibraryWebEcosystemDefinition : Definition<BuildModel.None>, WebEcosystemDefinition
