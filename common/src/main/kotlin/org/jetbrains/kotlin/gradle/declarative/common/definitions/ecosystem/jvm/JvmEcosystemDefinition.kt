package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.jvm

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinJvmCompilationExtension

public interface JvmEcosystemDefinition {
    @get:Nested
    public val toolchain: JvmToolchain

    @get:Nested
    public val kotlin: KotlinJvmCompilationExtension
}

public interface JvmToolchain {
    public val releaseVersion: Property<Int>
    public val vendor: Property<JvmVendor>
    public val nativeImageCapable: Property<Boolean>
}