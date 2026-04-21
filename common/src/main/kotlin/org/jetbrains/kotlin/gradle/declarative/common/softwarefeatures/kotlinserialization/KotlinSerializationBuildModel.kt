package org.jetbrains.kotlin.gradle.declarative.common.softwarefeatures.kotlinserialization

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.features.binding.BuildModel


@Suppress("UnstableApiUsage")
public interface KotlinSerializationBuildModel : BuildModel {
    public val version: Property<String>
    public val enabledFormats: SetProperty<KotlinSerializationFormats>
}

public enum class KotlinSerializationFormats {
    JSON, PROTOBUF, CBOR, HOCON, PROPERTIES;
}