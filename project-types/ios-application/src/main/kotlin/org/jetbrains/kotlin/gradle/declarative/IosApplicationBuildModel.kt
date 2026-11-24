package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.features.binding.BuildModel
import org.jetbrains.kotlin.gradle.declarative.common.definitions.IosSubplatform

@Suppress("UnstableApiUsage")
public interface IosApplicationBuildModel : BuildModel {

    public val enabledSubplatforms: SetProperty<IosSubplatform>

    public val productName: Property<String>

    public val productBundleIdentifier: Property<String>
}