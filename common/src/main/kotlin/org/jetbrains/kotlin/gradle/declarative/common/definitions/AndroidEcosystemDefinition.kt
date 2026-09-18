package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

public interface AndroidEcosystemDefinition {

    public val minSdk: Property<Int>

    public val compileSdk: Property<Int>

    public val compileSdkExtension: Property<Int>

    public val namespace: Property<String>

    @get:Nested
    public val compilerOptions: KotlinJvmCompilerOptions
}