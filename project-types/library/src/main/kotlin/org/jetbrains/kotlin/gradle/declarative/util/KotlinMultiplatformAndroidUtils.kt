package org.jetbrains.kotlin.gradle.declarative.util

import com.android.build.api.dsl.HasConfigurableValue
import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilationBuilder
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidPlatformTest
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.populateSpec
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryAndroidEcosystemDefinition
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryTestingAndroidEcosystemDefinition
import kotlin.collections.plusAssign

context(androidTarget: KotlinMultiplatformAndroidLibraryTarget)
internal fun LibraryAndroidEcosystemDefinition.configureAndroidPlatform() {
    minSdk.orNull?.let { version ->
        androidTarget.minSdk { version.populateSpec() }
    }
    compileSdk.orNull?.let { version ->
        androidTarget.compileSdk { version.populateSpec() }
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
            hostTest.targetSdk.orNull?.let { version ->
                targetSdk { version.populateSpec() }
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
            deviceTest.targetSdk.orNull?.let { version ->
                targetSdk { version.populateSpec() }
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