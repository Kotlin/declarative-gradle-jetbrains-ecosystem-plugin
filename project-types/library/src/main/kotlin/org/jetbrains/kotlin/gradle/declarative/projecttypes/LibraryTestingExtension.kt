package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension

public interface LibraryTestingExtension : TestingExtension {

    @get:Nested
    public val dependencies: LibraryTestingDependenciesExtension
}