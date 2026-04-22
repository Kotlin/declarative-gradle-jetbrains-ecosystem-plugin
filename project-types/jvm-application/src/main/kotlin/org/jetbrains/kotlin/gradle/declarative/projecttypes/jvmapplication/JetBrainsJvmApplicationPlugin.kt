@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.internal.DefaultJvmVendorSpec
import org.gradle.testing.base.TestingExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.tasks.DefaultKotlinJavaToolchain
import org.jetbrains.kotlin.gradle.plugin.mpp.baseModuleName
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJvmCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JavaJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationUnit
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.KotlinJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JvmToolchain
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.DefaultJvmApplicationBuildModel.DefaultJvmCompilationUnit.DefaultKotlinJvmCompilationType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.COMPILER_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectType(JetBrainsJvmApplicationPlugin.Binding::class)
public abstract class JetBrainsJvmApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Applying JetBrains JVM Application plugin to ${target.path}")
    }

    public class Binding : ProjectTypeBinding {
        override fun bind(builder: ProjectTypeBindingBuilder) {
            builder
                .bindProjectType("jvmApplication", JvmApplicationApplyAction::class)
                .withUnsafeApplyAction()
                .withUnsafeDefinition()
                .withBuildModelImplementationType(DefaultJvmApplicationBuildModel::class.java)
        }
    }

    internal abstract class JvmApplicationApplyAction :
        ProjectTypeApplyAction<JvmApplicationProjectType, JvmApplicationBuildModel> {

        // Unsafe service for apply action
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val projectLayout: ProjectLayout

        @get:Inject
        abstract val tasksContainer: TaskContainer

        @get:Inject
        abstract val javaToolchainService: JavaToolchainService

        @get:Inject
        abstract val objectFactory: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: JvmApplicationProjectType,
            buildModel: JvmApplicationBuildModel
        ) {
            // Recommended for now way to apply Gradle and 3rd party plugins, `apply(project)` method usage will be
            // forbidden in the future releases
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            val kotlinJvmExtension = project.extensions.getByType(KotlinJvmExtension::class.java)
            val mainJvmCompilationUnit = kotlinJvmExtension.bindMainCompilation(
                definition,
                buildModel as DefaultJvmApplicationBuildModel,
            )

            registerApplication(
                definition,
                buildModel,
                mainJvmCompilationUnit,
            )

            kotlinJvmExtension.bindTestCompilation(
                definition,
                buildModel,
            )
        }

        private fun KotlinJvmExtension.bindMainCompilation(
            definition: JvmApplicationProjectType,
            buildModel: DefaultJvmApplicationBuildModel,
        ): JvmCompilationUnit {
            val mainCompilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

            return buildModel.compilationUnits
                .create(KotlinCompilation.MAIN_COMPILATION_NAME) { kotlinJvmCompilationUnit ->
                    kotlinJvmCompilationUnit as DefaultJvmApplicationBuildModel.DefaultJvmCompilationUnit
                    kotlinJvmCompilationUnit.kotlinCompilation = mainCompilation

                    kotlinJvmCompilationUnit.jvmEcosystem.jdkToolchain.bindToolchainDefinition(definition.toolchain)
                    kotlinJvmCompilationUnit.jvmEcosystem.implementationConfiguration.dependencies.addAllLater(
                        definition.dependencies.implementation.dependencies
                    )
                    kotlinJvmCompilationUnit.jvmEcosystem.compileOnlyConfiguration.dependencies.addAllLater(
                        definition.dependencies.compileOnly.dependencies
                    )

                    val defaultOptions = objectFactory.newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                    defaultOptions.moduleName.convention(
                        project.baseModuleName()
                    )
                    syncKotlinJvmCompilerOptionsAsConvention(
                        from = definition.kotlin.compilerOptions,
                        into = compilerOptions,
                        fallback = defaultOptions,
                    )
                    // KGP does this before wiring above, and this loses toolchain information
                    // repeating it again
                    DefaultKotlinJavaToolchain.wireJvmTargetToToolchain(
                        compilerOptions,
                        project
                    )

                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "kotlin",
                        KotlinJvmCompilationType::class.java
                    ) { kotlinCompilation ->
                        (kotlinCompilation as DefaultKotlinJvmCompilationType).kotlinCompilerClasspathProvider =
                            mainCompilation.project.configurations.getByName(COMPILER_CLASSPATH_CONFIGURATION_NAME)

                        kotlinCompilation.kotlinJvmCompilerOptions = mainCompilation
                            .compileTaskProvider.get().compilerOptions as KotlinJvmCompilerOptions
                    }

                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "java",
                        JavaJvmCompilationType::class.java
                    ) { javaCompilation ->
                        javaCompilation.compileArguments
                            .convention(
                                definition.java.compilerOptions.compilerArgs.orElse(emptyList())
                            )
                        val javaExtension = project.extensions.getByType(JavaPluginExtension::class.java)
                        val javaCompileTaskName = javaExtension
                            .sourceSets
                            .getByName(kotlinJvmCompilationUnit.name)
                            .compileJavaTaskName

                        javaCompilation.javaCompiler.convention(javaToolchainService
                            .compilerFor {
                                it.bindToolchainDefinition(definition.toolchain)
                            }
                        )

                        project
                            .tasks
                            .named(javaCompileTaskName, JavaCompile::class.java) {
                                it.javaCompiler.convention(javaCompilation.javaCompiler)
                                it.options.compilerArgumentProviders.add {
                                    javaCompilation.compileArguments.get()
                                }
                            }
                    }
            }
        }

        private fun JavaToolchainSpec.bindToolchainDefinition(
            toolchain: JvmToolchain
        ) {
            languageVersion.convention(
                toolchain.releaseVersion
                    .map { JavaLanguageVersion.of(it) }
                    .orElse(JavaLanguageVersion.current())
            )
            vendor.convention(
                toolchain.vendor
                    .map { it.toVendorSpec() }
                    .orElse(DefaultJvmVendorSpec.any())
            )
            nativeImageCapable.convention(
                toolchain.nativeImageCapable.orElse(false)
            )
        }

        private fun registerApplication(
            definition: JvmApplicationProjectType,
            buildModel: JvmApplicationBuildModel,
            jvmCompilationUnit: JvmCompilationUnit,
        ) {
            buildModel.applications.create(jvmCompilationUnit.name) { application ->
                application as DefaultJvmApplication
                application.mainClassName.convention(definition.mainClass)
                application.applicationName.convention(definition.name.orElse(project.name))
                application.moduleName.convention(definition.moduleName)
                application.jvmArgs.convention(definition.jvmArgs)
                application.jdkLauncher.convention(
                    javaToolchainService.launcherFor {
                        it.bindToolchainDefinition(definition.toolchain)
                    }
                )
                application.runtimeOnlyConfiguration.dependencies
                    .addAllLater(definition.dependencies.runtimeOnly.dependencies)
                application.executionDirectory.convention(
                    projectLayout.buildDirectory.dir("application/${application.name}")
                )
            }
        }

        private fun KotlinJvmExtension.bindTestCompilation(
            definition: JvmApplicationProjectType,
            buildModel: DefaultJvmApplicationBuildModel,
        ) {
            val testCompilation = target.compilations.getByName(KotlinCompilation.TEST_COMPILATION_NAME)
            project.plugins.apply("jvm-test-suite")
            val jvmTestSuiteExtension = project.extensions.getByType(TestingExtension::class.java)
            jvmTestSuiteExtension.suites.named("test", JvmTestSuite::class.java) { jvmTestSuite ->
                if (definition.testing.useJunitPlatform.getOrElse(false)) {
                    jvmTestSuite.useJUnitJupiter()
                } else {
                    jvmTestSuite.useJUnit()
                }
                definition.dependencies.runtimeOnly.dependencies.getOrElse(emptySet()).forEach {
                    jvmTestSuite.dependencies.runtimeOnly.add(it)
                }
            }

            buildModel.compilationUnits
                .create(KotlinCompilation.TEST_COMPILATION_NAME) { kotlinJvmCompilationUnit ->
                    kotlinJvmCompilationUnit as DefaultJvmApplicationBuildModel.DefaultJvmCompilationUnit
                    kotlinJvmCompilationUnit.kotlinCompilation = testCompilation

                    kotlinJvmCompilationUnit.jvmEcosystem.jdkToolchain.bindToolchainDefinition(definition.toolchain)
                    kotlinJvmCompilationUnit.jvmEcosystem.implementationConfiguration.dependencies.addAllLater(
                        definition.testing.dependencies.implementation.dependencies
                    )
                    kotlinJvmCompilationUnit.jvmEcosystem.compileOnlyConfiguration.dependencies.addAllLater(
                        definition.testing.dependencies.compileOnly.dependencies
                    )

                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "kotlin",
                        KotlinJvmCompilationType::class.java
                    ) { kotlinCompilation ->
                        (kotlinCompilation as DefaultKotlinJvmCompilationType).kotlinCompilerClasspathProvider =
                            project.configurations.getByName(COMPILER_CLASSPATH_CONFIGURATION_NAME)

                        kotlinCompilation.kotlinJvmCompilerOptions =
                            testCompilation.compileTaskProvider.get().compilerOptions as KotlinJvmCompilerOptions
                    }

                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "java",
                        JavaJvmCompilationType::class.java
                    ) { javaCompilation ->
                        javaCompilation.compileArguments
                            .convention(
                                definition.java.compilerOptions.compilerArgs.orElse(emptyList())
                            )
                        val javaExtension = project.extensions.getByType(JavaPluginExtension::class.java)
                        val javaCompileTaskName = javaExtension
                            .sourceSets
                            .getByName(kotlinJvmCompilationUnit.name)
                            .compileJavaTaskName

                        javaCompilation.javaCompiler.convention(javaToolchainService
                            .compilerFor {
                                it.bindToolchainDefinition(definition.toolchain)
                            }
                        )

                        project
                            .tasks
                            .named(javaCompileTaskName, JavaCompile::class.java) {
                                it.javaCompiler.convention(javaCompilation.javaCompiler)
                                it.options.compilerArgumentProviders.add {
                                    javaCompilation.compileArguments.get()
                                }
                            }
                    }
                }
        }
    }
}
