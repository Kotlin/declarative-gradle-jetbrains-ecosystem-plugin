package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.provider.SetProperty
import org.gradle.features.binding.BuildModel
import org.jetbrains.kotlin.gradle.declarative.common.definitions.IosSubplatform

@Suppress("UnstableApiUsage")
public interface IosApplicationBuildModel : BuildModel {

    public val enabledSubplatforms: SetProperty<IosSubplatform>
}