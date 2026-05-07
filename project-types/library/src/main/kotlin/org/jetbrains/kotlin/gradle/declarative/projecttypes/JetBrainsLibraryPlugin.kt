package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectType(JetBrainsLibraryPlugin.Binding::class)
public class JetBrainsLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public abstract class Binding : ProjectTypeBinding {
        override fun bind(builder: ProjectTypeBindingBuilder) {
            builder
                .bindProjectType(
                    "library",
                    LibraryApplyAction::class
                )
                .withUnsafeApplyAction()
                .withUnsafeDefinition()
        }
    }

    @Suppress("OPT_IN_USAGE")
    internal abstract class LibraryApplyAction : ProjectTypeApplyAction<LibraryProjectType, LibraryBuildModel> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryProjectType,
            buildModel: LibraryBuildModel
        ) {
            logger.info("Applying 'library' project type")
            buildModel.enabledPlatforms.value(
                definition.platforms.map {
                    it.map { definitionValue ->
                        LibraryPlatforms.entries.find { it.name.equals(definitionValue, ignoreCase = true)  }
                            ?: throw GradleException(
                                "Platform '$definitionValue' is not available, available platforms:\n" +
                                        LibraryPlatforms.entries.joinToString { it.name }
                            )
                    }
                }
            )

            applyKotlinPlugin(buildModel.enabledPlatforms.get().toSet())

            definition.dependencies.wireDependencies(buildModel.enabledPlatforms.get().toSet())
        }

        private fun applyKotlinPlugin(
            enabledPlatforms: Set<LibraryPlatforms>
        ) {
            when {
                enabledPlatforms.isEmpty() -> throw GradleException(
                    "No library platform was specified. Please specify at least one of ${LibraryPlatforms.entries.joinToString { it.name }}"
                )
                enabledPlatforms.size == 1 && enabledPlatforms == setOf(LibraryPlatforms.jvm) -> {
                    logger.info("Enabling Kotlin/JVM plugin")
                    pluginManager.apply("org.jetbrains.kotlin.jvm")
                }
                enabledPlatforms.size == 2 && enabledPlatforms == setOf(LibraryPlatforms.jvm, LibraryPlatforms.common) -> {
                    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
                    withKmpPlugin {
                        applyDefaultHierarchyTemplate()
                        jvm()
                        logger.info("Enabling Kotlin/KMP plugin with 'jvm()' target")
                    }
                }
                else -> {
                    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
                    withKmpPlugin {
                        logger.info("Enabling Kotlin/KMP plugin for the following platforms: ${enabledPlatforms.joinToString { it.name }}")
                        applyDefaultHierarchyTemplate()
                        enabledPlatforms.forEach { platform ->
                            when (platform) {
                                LibraryPlatforms.jvm -> jvm()
                                LibraryPlatforms.common -> logger.warn("'common' platform is only used in conjunction with 'jvm' platform. Please remove it.")
                                LibraryPlatforms.web -> {
                                    js { browser() }
                                    wasmJs { browser() }
                                }

                                LibraryPlatforms.ios -> {
                                    iosArm64()
                                    iosSimulatorArm64()
                                    iosX64()
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun LibraryDependenciesExtension.wireDependencies(
            enabledPlatforms: Set<LibraryPlatforms>
        ) {
            val configurations = this@LibraryApplyAction.project.configurations
            withJvmPlugin {
                @Suppress("UNCHECKED_CAST")
                val mainCompilation = (target as KotlinWithJavaTarget<*, KotlinJvmCompilerOptions>).compilations
                    .getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

                val apiConfiguration = configurations.getByName(mainCompilation.apiConfigurationName)
                val implementationConfiguration = configurations
                    .getByName(mainCompilation.implementationConfigurationName)
                val compileOnlyConfiguration = configurations.getByName(mainCompilation.compileOnlyConfigurationName)
                val runtimeOnlyConfiguration = configurations.getByName(mainCompilation.runtimeOnlyConfigurationName)

                apiConfiguration.fromDependencyCollector(api)
                apiConfiguration.fromDependencyCollector(jvmPlatform.api)
                implementationConfiguration.fromDependencyCollector(implementation)
                implementationConfiguration.fromDependencyCollector(jvmPlatform.implementation)
                compileOnlyConfiguration.fromDependencyCollector(jvmPlatform.compileOnly)
                runtimeOnlyConfiguration.fromDependencyCollector(jvmPlatform.runtimeOnly)
            }

            withKmpPlugin {
                val commonSourceSet = sourceSets.getByName("commonMain")

                addDependencies(commonSourceSet.apiConfigurationName, api)
                addDependencies(commonSourceSet.implementationConfigurationName, implementation)

                if (enabledPlatforms.contains(LibraryPlatforms.jvm)) {
                    val jvmMainSourceSet = sourceSets.getByName("jvmMain")
                    addDependencies(jvmMainSourceSet.apiConfigurationName, jvmPlatform.api)
                    addDependencies(jvmMainSourceSet.implementationConfigurationName, jvmPlatform.implementation)
                    addDependencies(jvmMainSourceSet.compileOnlyConfigurationName, jvmPlatform.compileOnly)
                    addDependencies(jvmMainSourceSet.runtimeOnlyConfigurationName, jvmPlatform.runtimeOnly)
                }

                if (enabledPlatforms.contains(LibraryPlatforms.web)) {
                    val webMainSourceSet = sourceSets.getByName("webMain")

                    addDependencies(webMainSourceSet.apiConfigurationName, webPlatform.api)
                    addDependencies(webMainSourceSet.implementationConfigurationName, webPlatform.implementation)
                }

                if (enabledPlatforms.contains(LibraryPlatforms.ios)) {
                    val iosMainSourceSet = sourceSets.getByName("iosMain")

                    addDependencies(iosMainSourceSet.apiConfigurationName, iosPlatformDependencies.api)
                    addDependencies(iosMainSourceSet.implementationConfigurationName, iosPlatformDependencies.implementation)
                }
            }
        }

        private fun withJvmPlugin(action: KotlinJvmExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                val kotlinJvmExtension = project.extensions.getByType(KotlinJvmExtension::class.java)
                action(kotlinJvmExtension)
            }
        }

        private fun withKmpPlugin(action: KotlinMultiplatformExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                action(kmpExtension)
            }
        }

        private fun addDependencies(name: String, dependencies: DependencyCollector) {
            project.configurations.getByName(name).fromDependencyCollector(dependencies)
        }
    }
}