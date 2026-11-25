package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidDeviceTest : AndroidPlatformTest {

    public val applicationId: Property<String>

    public val handleProfiling: Property<Boolean>

    public val functionalTest: Property<Boolean>

    public val enableCoverage: Property<Boolean>
}