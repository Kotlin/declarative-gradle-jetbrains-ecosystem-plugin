package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.plugins.jvm.TestFixturesDependencyModifiers

public interface JvmApplicationTestingDependenciesExtension :
    JvmApplicationDependenciesExtension,
    TestFixturesDependencyModifiers
