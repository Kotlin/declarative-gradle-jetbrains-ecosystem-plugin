package org.jetbrains.kotlin.gradle.declarative.util

import com.android.build.api.dsl.CompileSdkSpec
import com.android.build.api.dsl.HasConfigurableValue
import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilationBuilder
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.api.dsl.MinSdkSpec
import com.android.build.api.dsl.TargetSdkSpec
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidPlatformTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.CompileSdkVersion
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.MinSdkVersion
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.SdkVersion
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.TargetSdkVersion
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryAndroidEcosystemDefinition
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryTestingAndroidEcosystemDefinition
import kotlin.collections.plusAssign

internal sealed interface SdkVersionImpl : SdkVersion {

    data class Release(override val apiLevel: Int) : SdkVersion.Release, MinSdkVersionImpl, CompileSdkVersionImpl, TargetSdkVersionImpl {

        context(spec: MinSdkSpec)
        override fun populateMinSdkVersionSpec() = spec.run {
            version = release(apiLevel)
        }

        context(spec: CompileSdkSpec)
        override fun populateCompileSdkVersionSpec() = spec.run {
            version = release(apiLevel)
        }

        context(spec: TargetSdkSpec)
        override fun populateTargetSdkVersionSpec() = spec.run {
            version = release(apiLevel)
        }
    }

    data class Preview(override val codeName: String) : SdkVersion.Preview, MinSdkVersionImpl, CompileSdkVersionImpl, TargetSdkVersionImpl {

        context(spec: MinSdkSpec)
        override fun populateMinSdkVersionSpec() = spec.run {
            version = preview(codeName)
        }

        context(spec: CompileSdkSpec)
        override fun populateCompileSdkVersionSpec() = spec.run {
            version = preview(codeName)
        }

        context(spec: TargetSdkSpec)
        override fun populateTargetSdkVersionSpec() = spec.run {
            version = preview(codeName)
        }
    }
}

internal sealed interface MinSdkVersionImpl : SdkVersionImpl, MinSdkVersion {

    context(spec: MinSdkSpec)
    fun populateMinSdkVersionSpec()
}

internal sealed interface CompileSdkVersionImpl : SdkVersionImpl, CompileSdkVersion {

    context(spec: CompileSdkSpec)
    fun populateCompileSdkVersionSpec()

    data class CompileRelease(val apiLevel: Int, val minorApiLevel: Int?, val apiLevelExtension: Int?) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateCompileSdkVersionSpec() = spec.run {
            version = release(apiLevel) {
                minorApiLevel = this@CompileRelease.minorApiLevel
                sdkExtension = apiLevelExtension
            }
        }
    }

    data class Canary(val canaryDate: String) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateCompileSdkVersionSpec() = spec.run {
            version = canary(canaryDate)
        }
    }

    data class Beta(val apiLevel: Int, val minorApiLevel: Int?, val betaVersion: Int?) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateCompileSdkVersionSpec() = spec.run {
            version = beta(apiLevel) {
                minorApiLevel = this@Beta.minorApiLevel
                betaVersion = this@Beta.betaVersion
            }
        }
    }

    data class Addon(val vendorName: String, val addonName: String, val apiLevel: Int) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateCompileSdkVersionSpec() = spec.run {
            version = addon(vendorName, addonName, apiLevel)
        }
    }
}

internal sealed interface TargetSdkVersionImpl : SdkVersionImpl, TargetSdkVersion {

    context(spec: TargetSdkSpec)
    fun populateTargetSdkVersionSpec()
}

context(androidTarget: KotlinMultiplatformAndroidLibraryTarget)
internal fun LibraryAndroidEcosystemDefinition.configureAndroidPlatform() {
    minSdk.orNull?.let {
        androidTarget.minSdk {
            (it as MinSdkVersionImpl).populateMinSdkVersionSpec()
        }
    }
    compileSdk.orNull?.let {
        androidTarget.compileSdk {
            (it as CompileSdkVersionImpl).populateCompileSdkVersionSpec()
        }
    }
    namespace.orNull?.let { androidTarget.namespace = it }
    androidResources.enable.orNull?.let { androidTarget.androidResources.enable = it }
    androidResources.resourcePrefix.orNull?.let { androidTarget.androidResources.resourcePrefix = it }
    androidResources.ignoreAssetsPatterns.orNull?.let { androidTarget.androidResources.ignoreAssetsPatterns += it }
    androidResources.noCompress.orNull?.let { androidTarget.androidResources.noCompress += it }
    androidResources.failOnMissingConfigEntry.orNull?.let { androidTarget.androidResources.failOnMissingConfigEntry = it }
    androidResources.additionalParameters.orNull?.let { androidTarget.androidResources.additionalParameters += it }
}

context(androidTarget: KotlinMultiplatformAndroidLibraryTarget)
internal fun LibraryTestingAndroidEcosystemDefinition.configureAndroidTesting() {
    hostTest.configureAndroidPlatformTest(
        builder = androidTarget::withHostTestBuilder,
        defaultBuilder = androidTarget::withHostTest,
        action = {
            hostTest.targetSdk.orNull?.let {
                targetSdk {
                    (it as TargetSdkVersionImpl).populateTargetSdkVersionSpec()
                }
            }
            hostTest.returnDefaultValues.orNull?.let { isReturnDefaultValues = it }
            hostTest.includeAndroidResources.orNull?.let { isIncludeAndroidResources = it }
            hostTest.enableCoverage.orNull?.let { enableCoverage = it }
        }
    )
    deviceTest.configureAndroidPlatformTest(
        builder = androidTarget::withDeviceTestBuilder,
        defaultBuilder = androidTarget::withDeviceTest,
        action = {
            deviceTest.targetSdk.orNull?.let {
                targetSdk {
                    (it as TargetSdkVersionImpl).populateTargetSdkVersionSpec()
                }
            }
            deviceTest.applicationId.orNull?.let { applicationId = it }
            deviceTest.instrumentationRunner.orNull?.let { instrumentationRunner = it }
            deviceTest.instrumentationRunnerArguments.orNull?.let { instrumentationRunnerArguments += it }
            deviceTest.handleProfiling.orNull?.let { handleProfiling = it }
            deviceTest.functionalTest.orNull?.let { functionalTest = it }
            deviceTest.enableCoverage.orNull?.let { enableCoverage = it }
        }
    )
}

private inline fun <T> AndroidPlatformTest.configureAndroidPlatformTest(
    builder: (action: KotlinMultiplatformAndroidCompilationBuilder.() -> Unit) -> HasConfigurableValue<T>,
    defaultBuilder: (T.() -> Unit) -> Unit,
    noinline action: T.() -> Unit,
) = sourceSetTreeName.orNull?.let {
    builder { sourceSetTreeName = it }.configure(action)
} ?: defaultBuilder(action)