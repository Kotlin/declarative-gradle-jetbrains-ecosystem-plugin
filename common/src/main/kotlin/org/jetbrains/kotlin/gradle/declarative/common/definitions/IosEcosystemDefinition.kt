package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested

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