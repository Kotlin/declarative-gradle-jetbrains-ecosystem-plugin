package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property

public interface AndroidPlatformTestBuilder {

    public val sourceSetTreeName: Property<String>
}