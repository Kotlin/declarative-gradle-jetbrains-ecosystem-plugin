package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import com.android.build.api.dsl.CompileSdkSpec
import com.android.build.api.dsl.MinSdkSpec
import com.android.build.api.dsl.TargetSdkSpec

public sealed interface SdkVersion {

    public sealed interface Release : SdkVersion, MinSdkVersion, CompileSdkVersion, TargetSdkVersion {
        public val apiLevel: Int
    }

    public sealed interface Preview : SdkVersion, MinSdkVersion, CompileSdkVersion, TargetSdkVersion {
        public val codeName: String
    }
}

public sealed interface MinSdkVersion : SdkVersion

context(spec: MinSdkSpec)
public fun MinSdkVersion.populateSpec(): Unit = (this as MinSdkVersionImpl).populateSpec()

public sealed interface CompileSdkVersion : SdkVersion

context(spec: CompileSdkSpec)
public fun CompileSdkVersion.populateSpec(): Unit = (this as CompileSdkVersionImpl).populateSpec()

public sealed interface TargetSdkVersion : SdkVersion

context(spec: TargetSdkSpec)
public fun TargetSdkVersion.populateSpec(): Unit = (this as TargetSdkVersionImpl).populateSpec()