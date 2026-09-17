package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.testfixtures

import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface TestFixturesDefinition : Definition<BuildModel.None> {
    @get:Nested
    public val dependencies: JvmLibraryDependencies
}
