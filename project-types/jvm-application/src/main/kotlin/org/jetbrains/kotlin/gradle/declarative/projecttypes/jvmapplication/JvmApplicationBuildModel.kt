package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.features.binding.BuildModel
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JavaJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationUnit
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmEcosystem
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.KotlinJvmCompilationType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface JvmApplicationBuildModel : BuildModel {
    public val compilationUnits: NamedDomainObjectContainer<JvmCompilationUnit>
    public val applications: NamedDomainObjectContainer<JvmApplication>
}

public interface JvmApplication : Named {
    public val mainClassName: Property<String>
    public val applicationName: Property<String>
    public val moduleName: Property<String>
    public val jvmArgs: ListProperty<String>
    public val jdkLauncher: Property<JavaLauncher>

    public val compiledClasses: ConfigurableFileCollection
    public val runtimeOnlyConfiguration: Configuration
    public val runtimeClasspath: FileCollection

    public val runTask: TaskProvider<JavaExec>
    public val executionDirectory: DirectoryProperty
}

internal interface InternalJvmApplication : JvmApplication {
    var runTaskProvider: TaskProvider<JavaExec>
    override val runTask: TaskProvider<JavaExec>
        get() = runTaskProvider

    var runtimeOnlyConfigurationProvider: Configuration
    override val runtimeOnlyConfiguration: Configuration
        get() = runtimeOnlyConfigurationProvider

    var runtimeClasspathProvider: FileCollection
    override val runtimeClasspath: FileCollection
        get() = runtimeClasspathProvider
}

internal abstract class DefaultJvmApplicationBuildModel @Inject constructor(
    private val objectFactory: ObjectFactory,
) : JvmApplicationBuildModel {
    override val compilationUnits: NamedDomainObjectContainer<JvmCompilationUnit> =
        objectFactory.domainObjectContainer(JvmCompilationUnit::class.java) { name ->
            objectFactory.newInstance(DefaultJvmCompilationUnit::class.java, name)
        }

    @Suppress("UNCHECKED_CAST")
    override val applications: NamedDomainObjectContainer<JvmApplication> = objectFactory
        .domainObjectContainer(InternalJvmApplication::class.java) as NamedDomainObjectContainer<JvmApplication>

    abstract class DefaultJvmCompilationUnit @Inject constructor(
        private val entityName: String,
        private val objectFactory: ObjectFactory,
        providerFactory: ProviderFactory,
    ) : JvmCompilationUnit {
        override fun getName(): String = entityName

        lateinit var kotlinCompilation: KotlinCompilation<*>

        override val sources: SourceDirectorySet
            get() = kotlinCompilation.defaultSourceSet.kotlin

        override val destinationDirectory: DirectoryProperty
            get() = kotlinCompilation.defaultSourceSet.kotlin.destinationDirectory

        override val outputs: FileCollection
            get() = kotlinCompilation.output.classesDirs

        override val resourcesOutput: Provider<Directory>
            get() = objectFactory.directoryProperty().fileValue(kotlinCompilation.output.resourcesDir)

        override val jvmEcosystem: JvmEcosystem =
            objectFactory.newInstance(DefaultJvmEcosystem::class.java, this)

        override val jvmCompilations: PolymorphicDomainObjectContainer<JvmCompilationType> =
            objectFactory
                .polymorphicDomainObjectContainer(JvmCompilationType::class.java)
                .apply {
                    registerFactory(KotlinJvmCompilationType::class.java) { name ->
                        objectFactory.newInstance(DefaultKotlinJvmCompilationType::class.java, name)
                    }
                    registerFactory(JavaJvmCompilationType::class.java) { name ->
                        objectFactory.newInstance(DefaultJavaJvmCompilationType::class.java, name)
                    }
                }

        abstract class DefaultJvmEcosystem @Inject constructor(
            private val compilationUnit: DefaultJvmCompilationUnit
        ) : JvmEcosystem {
            private val kotlinCompilation
                get() = compilationUnit.kotlinCompilation

            override val implementationConfiguration: Configuration
                get() = kotlinCompilation.project.configurations
                    .getByName(kotlinCompilation.defaultSourceSet.implementationConfigurationName)

            override val compileOnlyConfiguration: Configuration
                get() = kotlinCompilation.project.configurations
                    .getByName(kotlinCompilation.defaultSourceSet.compileOnlyConfigurationName)

            override val compilationClasspath: FileCollection
                get() = kotlinCompilation.compileDependencyFiles

            override val jdkToolchain: JavaToolchainSpec
                get() = kotlinCompilation.project.extensions.getByType(JavaPluginExtension::class.java).toolchain
        }

        abstract class DefaultKotlinJvmCompilationType @Inject constructor(
            override val compilationName: String
        ) : KotlinJvmCompilationType

        abstract class DefaultJavaJvmCompilationType @Inject constructor(
            override val compilationName: String
        ) : JavaJvmCompilationType
    }
}

