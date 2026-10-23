package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import com.android.build.api.dsl.CompileSdkSpec
import com.android.build.api.dsl.MinSdkSpec
import com.android.build.api.dsl.TargetSdkSpec

internal sealed interface SdkVersionImpl : SdkVersion {

    data class Release(override val apiLevel: Int) : SdkVersion.Release, MinSdkVersionImpl, CompileSdkVersionImpl, TargetSdkVersionImpl {

        context(spec: MinSdkSpec)
        override fun populateSpec() = spec.run {
            version = release(apiLevel)
        }

        context(spec: CompileSdkSpec)
        override fun populateSpec() = spec.run {
            version = release(apiLevel)
        }

        context(spec: TargetSdkSpec)
        override fun populateSpec() = spec.run {
            version = release(apiLevel)
        }
    }

    data class Preview(override val codeName: String) : SdkVersion.Preview, MinSdkVersionImpl, CompileSdkVersionImpl, TargetSdkVersionImpl {

        context(spec: MinSdkSpec)
        override fun populateSpec() = spec.run {
            version = preview(codeName)
        }

        context(spec: CompileSdkSpec)
        override fun populateSpec() = spec.run {
            version = preview(codeName)
        }

        context(spec: TargetSdkSpec)
        override fun populateSpec() = spec.run {
            version = preview(codeName)
        }
    }
}

internal sealed interface MinSdkVersionImpl : SdkVersionImpl, MinSdkVersion {

    context(spec: MinSdkSpec)
    fun populateSpec()
}

internal sealed interface CompileSdkVersionImpl : SdkVersionImpl, CompileSdkVersion {

    context(spec: CompileSdkSpec)
    fun populateSpec()

    data class Release(val apiLevel: Int, val minorApiLevel: Int?, val apiLevelExtension: Int?) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateSpec() = spec.run {
            version = release(apiLevel) {
                minorApiLevel = this@Release.minorApiLevel
                sdkExtension = apiLevelExtension
            }
        }
    }

    data class Canary(val canaryDate: String) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateSpec() = spec.run {
            version = canary(canaryDate)
        }
    }

    data class Beta(val apiLevel: Int, val minorApiLevel: Int?, val betaVersion: Int?) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateSpec() = spec.run {
            version = beta(apiLevel) {
                minorApiLevel = this@Beta.minorApiLevel
                betaVersion = this@Beta.betaVersion
            }
        }
    }

    data class Addon(val vendorName: String, val addonName: String, val apiLevel: Int) : CompileSdkVersionImpl {
        context(spec: CompileSdkSpec)
        override fun populateSpec() = spec.run {
            version = addon(vendorName, addonName, apiLevel)
        }
    }
}

internal sealed interface TargetSdkVersionImpl : SdkVersionImpl, TargetSdkVersion {

    context(spec: TargetSdkSpec)
    fun populateSpec()
}