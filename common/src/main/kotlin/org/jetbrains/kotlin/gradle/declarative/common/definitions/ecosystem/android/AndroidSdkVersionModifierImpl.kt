package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

internal abstract class AndroidSdkVersionModifierImpl : AndroidSdkVersionModifiers {

    override fun release(version: Int): SdkVersion.Release = SdkVersionImpl.Release(version)

    override fun preview(codeName: String): SdkVersion.Preview = SdkVersionImpl.Preview(codeName)
}

internal abstract class AndroidCompileSdkVersionModifiersImpl : AndroidCompileSdkVersionModifiers {

    override fun release(version: Int, minorVersion: Int?, extension: Int?): CompileSdkVersion =
        CompileSdkVersionImpl.Release(
            apiLevel = version,
            minorApiLevel = minorVersion,
            apiLevelExtension = extension
        )

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