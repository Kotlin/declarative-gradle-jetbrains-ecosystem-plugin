package org.jetbrains.kotlin.gradle.declarative.projecttype

import org.gradle.api.provider.Property

public interface LibraryPublishingExtension : LibraryDefinition {
    public val group: Property<String>
    public val version: Property<String>
 }