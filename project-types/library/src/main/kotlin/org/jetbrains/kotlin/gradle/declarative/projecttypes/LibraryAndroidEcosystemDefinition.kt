package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.model.ObjectFactory
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidCompileSdkVersionModifiers
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidEcosystemDefinition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidSdkVersionModifiers
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.newCompileSdkVersionModifiers
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.newSdkVersionModifiers
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface LibraryAndroidEcosystemDefinition : Definition<BuildModel.None>, AndroidEcosystemDefinition

internal abstract class DefaultLibraryAndroidEcosystemDefinition @Inject constructor(objectFactory: ObjectFactory) :
    LibraryAndroidEcosystemDefinition,
    AndroidSdkVersionModifiers by objectFactory.newSdkVersionModifiers(),
    AndroidCompileSdkVersionModifiers by objectFactory.newCompileSdkVersionModifiers()