package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidHostTest : AndroidPlatformTestBuilder {

    public val returnDefaultValues: Property<Boolean>

    public val includeAndroidResources: Property<Boolean>

    public val enableCoverage: Property<Boolean>

    public val targetSdk: Property<Int>

    public val targetSdkPreview: Property<String>
}