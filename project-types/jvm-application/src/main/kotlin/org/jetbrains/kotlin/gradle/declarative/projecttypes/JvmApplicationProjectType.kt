package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.buildmodels.JvmApplicationBuildModel

public interface JvmApplicationProjectType : Definition<JvmApplicationBuildModel> {
}