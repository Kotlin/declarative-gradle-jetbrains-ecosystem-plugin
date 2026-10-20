package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinJvmCompilationExtension

public interface AndroidEcosystemDefinition {

    public val minSdk: Property<MinSdkVersion>

    public val compileSdk: Property<CompileSdkVersion>

    public val namespace: Property<String>

    @get:Nested
    public val androidResources: AndroidResources

    @get:Nested
    public val kotlin: KotlinJvmCompilationExtension

    public fun release(version: Int): SdkVersion.Release

    public fun preview(codeName: String): SdkVersion.Preview

    public fun release(version: Int, minorVersion: Int? = null, extension: Int? = null): CompileSdkVersion

    public fun canary(date: String): CompileSdkVersion

    public fun beta(version: Int, minorVersion: Int? = null, betaVersion: Int? = null): CompileSdkVersion

    public fun addon(vendor: String, name: String, version: Int): CompileSdkVersion
}