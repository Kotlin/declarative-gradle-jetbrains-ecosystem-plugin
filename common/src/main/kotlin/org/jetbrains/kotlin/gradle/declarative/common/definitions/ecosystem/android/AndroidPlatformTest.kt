package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidPlatformTest {

    public val sourceSetTreeName: Property<String>

    public val targetSdk: Property<TargetSdkVersion>

    public fun release(version: Int): SdkVersion.Release

    public fun preview(codeName: String): SdkVersion.Preview
}