package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

public interface AndroidEcosystemDefinition {

    //TODO: figure out how to force users to set either minSdk or minSdkPreview exclusively, same goes for compileSdk
    public val minSdk: Property<Int>

    public val minSdkPreview: Property<String>

    public val compileSdk: Property<Int>

    public val compileSdkExtension: Property<Int>

    public val compileSdkPreview: Property<String>

    public val namespace: Property<String>

    @get:Nested
    public val androidResources: AndroidResources

    @get:Nested
    public val compilerOptions: KotlinJvmCompilerOptions
}