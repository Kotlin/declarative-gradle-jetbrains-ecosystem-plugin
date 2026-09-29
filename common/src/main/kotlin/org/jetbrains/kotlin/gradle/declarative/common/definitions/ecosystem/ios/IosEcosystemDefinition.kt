package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.ios

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.EcosystemWithSubplatforms
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.Subplatform
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinNativeCompilationExtension

public interface IosEcosystemDefinition : EcosystemWithSubplatforms<IosSubplatform> {
    @get:Nested
    public val kotlin: KotlinNativeCompilationExtension

    /**
     * See [IosSubplatform] for available values.
     */
    public override val subplatforms: ListProperty<IosSubplatform>
}

public enum class IosSubplatform : Subplatform {
    iosArm64, iosSimulatorArm64, iosX64;
}