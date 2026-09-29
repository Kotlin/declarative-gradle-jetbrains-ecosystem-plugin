package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.ios

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinNativeCompilationExtension

public interface IosEcosystemDefinition {
    @get:Nested
    public val kotlin: KotlinNativeCompilationExtension

    /**
     * See [IosSubplatforms] for available values.
     */
    public val subplatforms: ListProperty<String>
}

public enum class IosSubplatforms {
    iosArm64, iosSimulatorArm64, iosX64;
}