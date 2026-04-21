package org.jetbrains.kotlin.gradle.declarative.common.softwarefeatures.kotlinserialization

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.features.binding.Definition

@Suppress("UnstableApiUsage")
public interface KotlinSerializationDefinition : Definition<KotlinSerializationBuildModel> {
    public val version: Property<String>
    // TODO: migrate to enums once https://github.com/gradle/gradle/issues/34114 is fixed
    public val enabledFormats: ListProperty<String>
}
