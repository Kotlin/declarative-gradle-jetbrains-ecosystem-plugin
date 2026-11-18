package org.jetbrains.kotlin.gradle.declarative.compiler.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
actual annotation class IosApplicationEntryPoint(actual val applicationName: String = "")
