package org.jetbrains.kotlin.gradle.declarative.projecttype

import org.gradle.api.plugins.jvm.TestFixturesDependencyModifiers

@Suppress("UnstableApiUsage")
public interface JvmApplicationTestingDependenciesExtension :
    JvmApplicationDependenciesExtension,
    TestFixturesDependencyModifiers
