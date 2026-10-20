package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.model.ObjectFactory
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidEcosystemDefinition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.CompileSdkVersion
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.SdkVersion
import org.jetbrains.kotlin.gradle.declarative.util.CompileSdkVersionImpl
import org.jetbrains.kotlin.gradle.declarative.util.SdkVersionImpl
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface LibraryAndroidEcosystemDefinition : Definition<BuildModel.None>, AndroidEcosystemDefinition

internal abstract class DefaultLibraryAndroidEcosystemDefinition
@Inject constructor(private val objectFactory: ObjectFactory) : LibraryAndroidEcosystemDefinition {

    override fun release(version: Int): SdkVersion.Release = SdkVersionImpl.Release(version)

    override fun release(version: Int, minorVersion: Int?, extension: Int?): CompileSdkVersion =
        CompileSdkVersionImpl.CompileRelease(
            apiLevel = version,
            minorApiLevel = minorVersion,
            apiLevelExtension = extension
        )

    override fun preview(codeName: String): SdkVersion.Preview = SdkVersionImpl.Preview(codeName)

    override fun canary(date: String): CompileSdkVersion = CompileSdkVersionImpl.Canary(date)

    override fun beta(version: Int, minorVersion: Int?, betaVersion: Int?): CompileSdkVersion =
        CompileSdkVersionImpl.Beta(
            apiLevel = version,
            minorApiLevel = minorVersion,
            betaVersion = betaVersion
        )

    override fun addon(vendor: String, name: String, version: Int): CompileSdkVersion =
        CompileSdkVersionImpl.Addon(vendor, name, version)
}