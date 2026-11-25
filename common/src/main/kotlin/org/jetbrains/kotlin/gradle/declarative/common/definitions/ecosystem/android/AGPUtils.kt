package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

public inline fun whenAGPAvailable(action: () -> Unit) {
    try {
        Class.forName("com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension")
        action()
    } catch (_: ClassNotFoundException) {

    }
}