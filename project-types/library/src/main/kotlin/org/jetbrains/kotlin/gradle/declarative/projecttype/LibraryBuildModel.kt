package org.jetbrains.kotlin.gradle.declarative.projecttype

import org.gradle.api.provider.SetProperty
import org.gradle.features.binding.BuildModel
import org.jetbrains.kotlin.gradle.declarative.common.definitions.IosSubplatform
import org.jetbrains.kotlin.gradle.declarative.common.definitions.WebSubplatform


@Suppress("UnstableApiUsage")
public interface LibraryBuildModel : BuildModel {
    public val enabledPlatforms: SetProperty<LibraryPlatform>

    public val enabledWebSubplatforms: SetProperty<WebSubplatform>

    public val enabledIosSubplatforms: SetProperty<IosSubplatform>
}