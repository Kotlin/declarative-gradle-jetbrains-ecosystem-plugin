package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface TestingExtension<BuildModel : TestingBuildModel> : Definition<BuildModel>

public interface TestingBuildModel : BuildModel
