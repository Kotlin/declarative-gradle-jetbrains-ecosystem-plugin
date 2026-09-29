package org.jetbrains.kotlin.gradle.declarative.util

import com.android.build.api.dsl.HasConfigurableValue
import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilationBuilder
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.AndroidPlatformTestBuilder
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryTestingAndroidEcosystemDefinition
import kotlin.collections.plusAssign

context(androidTarget: KotlinMultiplatformAndroidLibraryTarget)
internal fun LibraryTestingAndroidEcosystemDefinition.configureAndroidTesting() {
    hostTest.configureAndroidPlatformTest(
        builder = androidTarget::withHostTestBuilder,
        defaultBuilder = androidTarget::withHostTest,
        action = {
            hostTest.returnDefaultValues.orNull?.let { isReturnDefaultValues = it }
            hostTest.includeAndroidResources.orNull?.let { isIncludeAndroidResources = it }
            hostTest.enableCoverage.orNull?.let { enableCoverage = it }
            hostTest.targetSdk.orNull?.let {
                targetSdk {
                    version = release(it)
                }
            } ?: hostTest.targetSdkPreview.orNull?.let {
                targetSdk {
                    version = preview(it)
                }
            }
        }
    )
    deviceTest.configureAndroidPlatformTest(
        builder = androidTarget::withDeviceTestBuilder,
        defaultBuilder = androidTarget::withDeviceTest,
        action = {
            deviceTest.targetSdk.orNull?.let {
                targetSdk {
                    version = release(it)
                }
            } ?: deviceTest.targetSdkPreview.orNull?.let {
                targetSdk {
                    version = preview(it)
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

private inline fun <T> AndroidPlatformTestBuilder.configureAndroidPlatformTest(
    builder: (action: KotlinMultiplatformAndroidCompilationBuilder.() -> Unit) -> HasConfigurableValue<T>,
    defaultBuilder: (T.() -> Unit) -> Unit,
    noinline action: T.() -> Unit,
) = sourceSetTreeName.orNull?.let {
    builder { sourceSetTreeName = it }.configure(action)
} ?: defaultBuilder(action)