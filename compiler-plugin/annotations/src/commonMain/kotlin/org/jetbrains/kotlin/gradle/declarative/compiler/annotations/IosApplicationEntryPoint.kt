package org.jetbrains.kotlin.gradle.declarative.compiler.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
expect annotation class IosApplicationEntryPoint(val applicationName: String)
