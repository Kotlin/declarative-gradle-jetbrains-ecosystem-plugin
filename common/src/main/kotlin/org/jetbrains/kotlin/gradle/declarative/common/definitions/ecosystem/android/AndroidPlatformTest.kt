package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidPlatformTest : AndroidSdkVersionModifiers {

    public val targetSdk: Property<TargetSdkVersion>
}