package org.jetbrains.kotlin.gradle.declarative.projectfeature.androidecosystem

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidDeviceTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidHostTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidSdkVersionModifiers
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.newSdkVersionModifiers
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface LibraryTestingAndroidEcosystemDefinition : Definition<BuildModel.None> {

    @get:Nested
    public val hostTest: AndroidHostTest

    @get:Nested
    public val deviceTest: AndroidDeviceTest
}

internal abstract class DefaultLibraryTestingAndroidEcosystemDefinition
@Inject constructor(objectFactory: ObjectFactory) : LibraryTestingAndroidEcosystemDefinition {

    @Nested
    override val hostTest: AndroidHostTest = objectFactory.newInstance(DefaultAndroidHostTest::class.java)

    @Nested
    override val deviceTest: AndroidDeviceTest = objectFactory.newInstance(DefaultAndroidDeviceTest::class.java)
}

internal abstract class DefaultAndroidHostTest @Inject constructor(objectFactory: ObjectFactory) : AndroidHostTest,
    AndroidSdkVersionModifiers by objectFactory.newSdkVersionModifiers()

internal abstract class DefaultAndroidDeviceTest @Inject constructor(objectFactory: ObjectFactory) : AndroidDeviceTest,
    AndroidSdkVersionModifiers by objectFactory.newSdkVersionModifiers()