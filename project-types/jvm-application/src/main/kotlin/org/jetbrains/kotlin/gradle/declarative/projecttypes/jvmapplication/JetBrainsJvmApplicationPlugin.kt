package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
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
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JavaJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JvmCompilationUnit
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.KotlinJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.definitions.JvmToolchain
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
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
                buildModel as DefaultJvmApplicationBuildModel
            )
            registerApplication(
                definition,
                buildModel,
                mainJvmCompilationUnit,
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
                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "kotlin",
                        KotlinJvmCompilationType::class.java
                    ) { kotlinCompilation ->
                        // TODO: wire properly
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
    }
}
