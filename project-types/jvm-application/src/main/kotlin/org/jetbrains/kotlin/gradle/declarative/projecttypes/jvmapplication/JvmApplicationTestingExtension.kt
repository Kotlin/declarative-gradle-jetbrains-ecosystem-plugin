package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationUnit
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingBuildModel
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension

public interface JvmApplicationTestingExtension : TestingExtension<JvmTestingBuildModel> {

    public val useJUnitPlatform: Property<Boolean>

    @get:Nested
    public val dependencies: JvmApplicationTestingDependenciesExtension
}

public interface JvmTestingBuildModel : TestingBuildModel {
    public val compilationUnit: JvmCompilationUnit
}

internal abstract class DefaultJvmTestingBuildModel : JvmTestingBuildModel {
    override lateinit var compilationUnit: JvmCompilationUnit
}
