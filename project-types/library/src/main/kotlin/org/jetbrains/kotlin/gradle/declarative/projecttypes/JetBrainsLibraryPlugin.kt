@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.internal.DefaultJvmVendorSpec
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinCommonCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJvmCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJsCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinNativeCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.DefaultKotlinJavaToolchain
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

            val enabledPlatforms = buildModel.enabledPlatforms.get().toSet()
            definition.dependencies.wireDependencies(enabledPlatforms)
            definition.wireKotlinCompilerOptions(context.objectFactory, enabledPlatforms)

            if (buildModel.enabledPlatforms.get().contains(LibraryPlatforms.jvm)) {
                definition.configureJvmPlatform()
            }
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

        private fun LibraryProjectType.configureJvmPlatform() {
            project.plugins.withType(JavaBasePlugin::class.java) {
                val toolchain = project.extensions.getByType(JavaPluginExtension::class.java).toolchain
                toolchain.languageVersion.convention(
                    jvmPlatform.toolchain.releaseVersion.map { JavaLanguageVersion.of(it) }
                        .orElse(JavaLanguageVersion.current())
                )
                toolchain.vendor.convention(jvmPlatform.toolchain.vendor.map { it.toVendorSpec() }
                    .orElse(DefaultJvmVendorSpec.any()))
                toolchain.nativeImageCapable.convention(
                    jvmPlatform.toolchain.nativeImageCapable.orElse(false)
                )
            }

            withJvmPlugin {
                @Suppress("UNCHECKED_CAST")
                val mainCompilation = (target as KotlinWithJavaTarget<*, KotlinJvmCompilerOptions>).compilations
                    .getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

                mainCompilation.compileJavaTaskProvider.configure {
                    it.options.compilerArgumentProviders.add {
                        java.compilerOptions.compilerArgs.get()
                    }
                }
            }

            withKmpPlugin {
                val jvmTarget = targets.getByName("jvm") as KotlinJvmTarget
                val mainCompilation = jvmTarget.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

                mainCompilation.compileJavaTaskProvider!!.configure {
                    it.options.compilerArgumentProviders.add {
                        java.compilerOptions.compilerArgs.get()
                    }
                }
            }
        }

        private fun LibraryProjectType.wireKotlinCompilerOptions(
            objectFactory: ObjectFactory,
            enabledPlatforms: Set<LibraryPlatforms>,
        ) {
            withJvmPlugin {
                val defaultJvmOptions = objectFactory.newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                val intermediateJvmOptions = objectFactory.newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                syncKotlinCommonCompilerOptionsAsConvention(
                    this@wireKotlinCompilerOptions.kotlin.compilerOptions,
                    intermediateJvmOptions,
                    defaultJvmOptions,
                )
                syncKotlinJvmCompilerOptionsAsConvention(
                    this@wireKotlinCompilerOptions.jvmPlatform.kotlin.compilerOptions,
                    compilerOptions,
                    intermediateJvmOptions
                )

                // KGP does this before wiring above, and this loses toolchain information
                // repeating it again
                DefaultKotlinJavaToolchain.wireJvmTargetToToolchain(
                    compilerOptions,
                    project
                )
            }

            withKmpPlugin {
                val defaultCommonOptions = objectFactory.newInstance(KotlinCommonCompilerOptionsDefault::class.java)
                syncKotlinCommonCompilerOptionsAsConvention(
                    this@wireKotlinCompilerOptions.kotlin.compilerOptions,
                    compilerOptions,
                    defaultCommonOptions,
                )

                if (enabledPlatforms.contains(LibraryPlatforms.jvm)) {
                    val jvmTarget = targets.getByName("jvm") as KotlinJvmTarget
                    val defaultJvmOptions = objectFactory.newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                    syncKotlinCommonCompilerOptionsAsConvention(
                        compilerOptions,
                        defaultJvmOptions,
                        defaultCommonOptions
                    )
                    syncKotlinJvmCompilerOptionsAsConvention(
                        this@wireKotlinCompilerOptions.jvmPlatform.kotlin.compilerOptions,
                        jvmTarget.compilerOptions,
                        defaultJvmOptions
                    )

                    // KGP does this before wiring above, and this loses toolchain information
                    // repeating it again
                    DefaultKotlinJavaToolchain.wireJvmTargetToToolchain(
                        jvmTarget.compilerOptions,
                        project
                    )
                }
                if (enabledPlatforms.contains(LibraryPlatforms.web)) {
                    val jsTarget = targets.getByName("js") as KotlinJsTargetDsl
                    val wasmJsTarget = targets.getByName("wasmJs") as KotlinWasmJsTargetDsl

                    val defaultJsOptions = objectFactory.newInstance(KotlinJsCompilerOptionsDefault::class.java)
                    syncKotlinCommonCompilerOptionsAsConvention(
                        compilerOptions,
                        defaultJsOptions,
                        defaultCommonOptions
                    )
                    syncKotlinJsCompilerOptionsAsConvention(
                        this@wireKotlinCompilerOptions.webPlatform.kotlin.compilerOptions,
                        jsTarget.compilerOptions,
                        defaultJsOptions
                    )
                    syncKotlinJsCompilerOptionsAsConvention(
                        this@wireKotlinCompilerOptions.webPlatform.kotlin.compilerOptions,
                        wasmJsTarget.compilerOptions,
                        defaultJsOptions
                    )
                }
                if (enabledPlatforms.contains(LibraryPlatforms.ios)) {
                    val defaultNativeOptions = objectFactory.newInstance(KotlinNativeCompilerOptionsDefault::class.java)
                    syncKotlinCommonCompilerOptionsAsConvention(
                        compilerOptions,
                        defaultNativeOptions,
                        defaultCommonOptions
                    )

                    val nativeTargets = listOf("iosArm64", "iosSimulatorArm64", "iosX64")
                        .map { targetName -> targets.getByName(targetName) as KotlinNativeTarget }
                    nativeTargets.forEach { nativeTarget ->
                        syncKotlinNativeCompilerOptionsAsConvention(
                            this@wireKotlinCompilerOptions.iosPlatform.kotlin.compilerOptions,
                            nativeTarget.compilerOptions,
                            defaultNativeOptions,
                        )
                    }
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