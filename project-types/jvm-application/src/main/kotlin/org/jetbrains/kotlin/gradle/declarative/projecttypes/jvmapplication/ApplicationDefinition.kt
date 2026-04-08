package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

public interface ApplicationDefinition {
    public val mainClass: Property<String>
    public val name: Property<String>
    public val moduleName: Property<String>
    public val jvmArgs: ListProperty<String>
}