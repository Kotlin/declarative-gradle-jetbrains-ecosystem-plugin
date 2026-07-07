package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.features.binding.BuildModel
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JavaJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationUnit
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmEcosystem
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.KotlinJvmCompilationType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
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

    public val jvmCompilationUnit: JvmCompilationUnit
    public val runtimeOnlyConfiguration: Configuration
    public val runtimeClasspath: FileCollection

    public val runTask: TaskProvider<JavaExec>
    public val jarTask: TaskProvider<Jar>
    public val executionDirectory: DirectoryProperty
}

internal abstract class DefaultJvmApplication @Inject constructor(
    override val jvmCompilationUnit: JvmCompilationUnit,
    private val objectFactory: ObjectFactory
): JvmApplication {
    override fun getName(): String = jvmCompilationUnit.name

    private val kotlinCompilation
        get() = (jvmCompilationUnit as DefaultJvmApplicationBuildModel.DefaultJvmCompilationUnit).kotlinCompilation

    override val runTask: TaskProvider<JavaExec> = kotlinCompilation.project.tasks.registerJvmApplicationRunTask()
    override val jarTask: TaskProvider<Jar> = kotlinCompilation.project.tasks.named(taskName("jar"), Jar::class.java)

    override val runtimeOnlyConfiguration: Configuration
        get() = kotlinCompilation.project.configurations
            .getByName(kotlinCompilation.defaultSourceSet.runtimeOnlyConfigurationName)

    override val runtimeClasspath: FileCollection
        get() = kotlinCompilation.runtimeDependencyFiles!!

    private fun TaskContainer.registerJvmApplicationRunTask(): TaskProvider<JavaExec> =
        register(taskName("run"), JavaExec::class.java) { javaExecTask ->
            javaExecTask.description = "Runs this project as a JVM application"
            javaExecTask.group = ApplicationPlugin.APPLICATION_GROUP

            val runtimeClasspath = objectFactory.fileCollection().from(
                jvmCompilationUnit.outputs,
                runtimeClasspath,
            )
            javaExecTask.classpath(runtimeClasspath)
            javaExecTask.mainClass.value(mainClassName)
            javaExecTask.mainModule.value(moduleName)
            javaExecTask.jvmArguments.value(jvmArgs)
            //TODO: javaExecTask.modularity.inferModulePath
            javaExecTask.javaLauncher.convention(jdkLauncher)
            javaExecTask.workingDir(
                executionDirectory.map {
                    it.asFile.mkdirs()
                    it
                }
            )
        }

    private fun JvmApplication.taskName(taskName: String) = if (name == KotlinCompilation.MAIN_COMPILATION_NAME) taskName else "$name${
        taskName.replaceFirstChar {
                if (it.isLowerCase()) it.uppercaseChar() else it
            }
        }"
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
        .domainObjectContainer(JvmApplication::class.java) { name ->
            val jvmCompilationUnit = compilationUnits.findByName(name)
                ?: throw IllegalStateException("Could not find compilation unit '$name' to create application")
            objectFactory.newInstance(DefaultJvmApplication::class.java, jvmCompilationUnit)
        }

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
            private val compilationName: String
        ) : KotlinJvmCompilationType {
            override fun getName(): String = compilationName

            internal lateinit var kotlinCompilerClasspathProvider: Configuration
            override val kotlinCompilerClasspath: Configuration
                get() = kotlinCompilerClasspathProvider

            internal lateinit var kotlinJvmCompilerOptions: KotlinJvmCompilerOptions
            override val compilerOptions: KotlinJvmCompilerOptions
                get() = kotlinJvmCompilerOptions
        }

        abstract class DefaultJavaJvmCompilationType @Inject constructor(
            private val compilationName: String
        ) : JavaJvmCompilationType {
            override fun getName(): String = compilationName
        }
    }
}
