package org.jetbrains.kotlin.gradle.declarative.common.buildtypes

import org.gradle.api.Named
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.jvm.toolchain.JavaCompiler
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

@Suppress("UnstableApiUsage")
public interface JvmEcosystem : BuildModel {
    public val implementationConfiguration: Configuration
    public val compileOnlyConfiguration: Configuration
    public val compilationClasspath: FileCollection
    public val jdkToolchain: JavaToolchainSpec
}

public sealed interface JvmCompilationType : Named

@Suppress("UnstableApiUsage")
public interface KotlinJvmCompilationType : JvmCompilationType, BuildModel {
    override fun getName(): String = "kotlin"

    public val kotlinCompilerClasspath: FileCollection

    @get:Nested
    public val compilerOptions: KotlinJvmCompilerOptions
}

public interface JavaJvmCompilationType : JvmCompilationType {
    override fun getName(): String = "java"

    // Missing Java compiler args
    public val javaCompiler: Property<JavaCompiler>

    public val compileArguments: ListProperty<String>
}

public interface JvmCompilationUnit : CompilationUnit {
    public val resourcesOutput: Provider<Directory>
    public val jvmEcosystem: JvmEcosystem
    public val jvmCompilations: PolymorphicDomainObjectContainer<JvmCompilationType>
}
