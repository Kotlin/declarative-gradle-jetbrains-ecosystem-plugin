package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidDeviceTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidHostTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidPlatformTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.SdkVersion
import org.jetbrains.kotlin.gradle.declarative.util.SdkVersionImpl
import javax.inject.Inject
import kotlin.jvm.java

public interface LibraryTestingExtension : TestingExtension {

    @get:Nested
    public val dependencies: LibraryTestingDependenciesExtension

    @get:Nested
    public val jvmPlatform: LibraryTestingJvmEcosystemDefinition

    @get:Nested
    public val webPlatform: LibraryTestingWebEcosystemDefinition

    @get:Nested
    public val iosPlatform: LibraryTestingIosEcosystemDefinition

    @get:Nested
    public val androidPlatform: LibraryTestingAndroidEcosystemDefinition
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingJvmEcosystemDefinition : Definition<BuildModel.None> {
    public val useJUnitPlatform: Property<Boolean>
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingWebEcosystemDefinition : Definition<BuildModel.None> {
    public val skip: Property<Boolean>
}

@Suppress("UnstableApiUsage")
public interface LibraryTestingIosEcosystemDefinition : Definition<BuildModel.None>

@Suppress("UnstableApiUsage")
public interface LibraryTestingAndroidEcosystemDefinition : Definition<BuildModel.None> {

    @get:Nested
    public val hostTest: AndroidHostTest

    @get:Nested
    public val deviceTest: AndroidDeviceTest
}

internal abstract class DefaultLibraryTestingExtension
@Inject constructor(objectFactory: ObjectFactory) : LibraryTestingExtension {

    override val androidPlatform: LibraryTestingAndroidEcosystemDefinition = objectFactory.newInstance(DefaultLibraryTestingAndroidEcosystemDefinition::class.java)
}

internal abstract class DefaultLibraryTestingAndroidEcosystemDefinition
@Inject constructor(objectFactory: ObjectFactory) : LibraryTestingAndroidEcosystemDefinition {

    override val hostTest: AndroidHostTest = objectFactory.newInstance(DefaultAndroidHostTest::class.java)

    override val deviceTest: AndroidDeviceTest = objectFactory.newInstance(DefaultAndroidDeviceTest::class.java)
}

internal abstract class DefaultAndroidPlatformTest @Inject constructor(objectFactory: ObjectFactory) : AndroidPlatformTest {

    override fun release(version: Int): SdkVersion.Release = SdkVersionImpl.Release(version)

    override fun preview(codeName: String): SdkVersion.Preview = SdkVersionImpl.Preview(codeName)
}

internal abstract class DefaultAndroidHostTest
@Inject constructor(objectFactory: ObjectFactory) : DefaultAndroidPlatformTest(objectFactory), AndroidHostTest

internal abstract class DefaultAndroidDeviceTest
@Inject constructor(objectFactory: ObjectFactory) : DefaultAndroidPlatformTest(objectFactory), AndroidDeviceTest
