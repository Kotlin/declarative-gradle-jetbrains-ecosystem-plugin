package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

public interface AndroidResources {

    public val enable: Property<Boolean>

    public val resourcePrefix: Property<String>

    public val ignoreAssetsPatterns: ListProperty<String>

    public val noCompress: ListProperty<String>

    public val failOnMissingConfigEntry: Property<Boolean>

    public val additionalParameters: ListProperty<String>
}