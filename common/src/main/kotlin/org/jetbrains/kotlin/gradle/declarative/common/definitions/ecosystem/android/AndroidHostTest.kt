package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidHostTest : AndroidPlatformTest {

    public val returnDefaultValues: Property<Boolean>

    public val includeAndroidResources: Property<Boolean>

    public val enableCoverage: Property<Boolean>
}