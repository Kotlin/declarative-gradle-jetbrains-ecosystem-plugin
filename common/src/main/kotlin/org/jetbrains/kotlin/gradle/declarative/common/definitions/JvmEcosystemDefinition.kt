package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public interface JvmEcosystemDefinition {
    @get:Nested
    public val toolchain: JvmToolchain
}

public interface JvmToolchain {
    public val releaseVersion: Property<Int>
    public val vendor: Property<JvmVendor>
    public val nativeImageCapable: Property<Boolean>
}