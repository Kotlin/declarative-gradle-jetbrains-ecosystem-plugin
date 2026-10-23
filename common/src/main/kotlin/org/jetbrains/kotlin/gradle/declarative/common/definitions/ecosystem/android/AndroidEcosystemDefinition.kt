package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinJvmCompilationExtension

public interface AndroidEcosystemDefinition : AndroidSdkVersionModifiers, AndroidCompileSdkVersionModifiers {

    public val minSdk: Property<MinSdkVersion>

    public val compileSdk: Property<CompileSdkVersion>

    public val namespace: Property<String>

    @get:Nested
    public val androidResources: AndroidResources

    @get:Nested
    public val kotlin: KotlinJvmCompilationExtension
}