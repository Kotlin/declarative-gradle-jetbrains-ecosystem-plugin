package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidPlatformTest : AndroidSdkVersionModifiers {

    public val sourceSetTreeName: Property<String>

    public val targetSdk: Property<TargetSdkVersion>
}