package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.model.ObjectFactory

public interface AndroidSdkVersionModifiers {

    public fun release(version: Int): SdkVersion.Release

    public fun preview(codeName: String): SdkVersion.Preview
}

public interface AndroidCompileSdkVersionModifiers {

    public fun release(version: Int, minorVersion: Int? = null, extension: Int? = null): CompileSdkVersion

    public fun canary(date: String): CompileSdkVersion

    public fun beta(version: Int, minorVersion: Int? = null, betaVersion: Int? = null): CompileSdkVersion

    public fun addon(vendor: String, name: String, version: Int): CompileSdkVersion
}

public fun ObjectFactory.newSdkVersionModifiers(): AndroidSdkVersionModifiers = newInstance(AndroidSdkVersionModifierImpl::class.java)

public fun ObjectFactory.newCompileSdkVersionModifiers(): AndroidCompileSdkVersionModifiers =
    newInstance(AndroidCompileSdkVersionModifiersImpl::class.java)