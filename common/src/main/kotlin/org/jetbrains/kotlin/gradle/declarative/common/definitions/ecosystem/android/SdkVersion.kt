package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

public interface SdkVersion {

    public interface Release : SdkVersion, MinSdkVersion, CompileSdkVersion, TargetSdkVersion {
        public val apiLevel: Int
    }

    public interface Preview : SdkVersion, MinSdkVersion, CompileSdkVersion, TargetSdkVersion {
        public val codeName: String
    }
}

public interface MinSdkVersion : SdkVersion

public interface CompileSdkVersion : SdkVersion

public interface TargetSdkVersion : SdkVersion