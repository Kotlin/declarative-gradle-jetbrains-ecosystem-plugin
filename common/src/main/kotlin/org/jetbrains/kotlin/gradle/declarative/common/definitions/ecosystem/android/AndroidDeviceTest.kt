package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

public interface AndroidDeviceTest : AndroidPlatformTest {

    public val applicationId: Property<String>

    public val instrumentationRunner: Property<String>

    public val instrumentationRunnerArguments: MapProperty<String, String>

    public val handleProfiling: Property<Boolean>

    public val functionalTest: Property<Boolean>

    public val enableCoverage: Property<Boolean>
}