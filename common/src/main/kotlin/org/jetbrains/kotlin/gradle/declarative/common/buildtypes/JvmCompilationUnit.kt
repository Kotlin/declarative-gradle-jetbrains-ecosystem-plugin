package org.jetbrains.kotlin.gradle.declarative.common.buildtypes

import org.gradle.api.Named
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.features.binding.BuildModel
import org.gradle.jvm.toolchain.JavaToolchainSpec

@Suppress("UnstableApiUsage")
public interface JvmEcosystem : BuildModel {
    public val implementationConfiguration: Configuration
    public val compileOnlyConfiguration: Configuration
    public val compilationClasspath: FileCollection
    public val jdkToolchain: JavaToolchainSpec
}

public sealed interface JvmCompilationType : Named {
    public val compilationName: String

    override fun getName(): String = compilationName
}
public interface KotlinJvmCompilationType : JvmCompilationType {
    // Missing Kotlin compiler args
    public val kotlinCompilerClasspath: ConfigurableFileCollection
}

public interface JavaJvmCompilationType : JvmCompilationType {
    // Missing Java compiler args
    public val javacPath: RegularFileProperty
}

public interface JvmCompilationUnit : CompilationUnit {
    public val resourcesOutput: Provider<Directory>
    public val jvmEcosystem: JvmEcosystem
    public val jvmCompilations: PolymorphicDomainObjectContainer<JvmCompilationType>
}
