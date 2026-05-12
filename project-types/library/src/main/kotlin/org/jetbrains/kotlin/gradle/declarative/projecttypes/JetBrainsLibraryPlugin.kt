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
import org.gradle.api.tasks.testing.Test
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.internal.DefaultJvmVendorSpec
import org.jetbrains.kotlin.gradle.declarative.common.definitions.IosSubplatforms
import org.jetbrains.kotlin.gradle.declarative.common.definitions.WebSubplatforms
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

        @get:Inject
        abstract val javaToolchainServices: JavaToolchainService

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

            val enabledPlatforms = buildModel.enabledPlatforms.get().toSet()
            val enabledWebSubplatforms = definition.webPlatform.subplatforms
                .map {
                    it.map { name ->
                        WebSubplatforms.entries.find { it.name.equals(name, ignoreCase = true) }
                            ?: throw GradleException(
                                "Unknown subplatform $name, accepted subplatforms: ${WebSubplatforms.entries.joinToString { it.name }}"
                            )
                    }
                }
                .getOrElse(WebSubplatforms.entries.toList())
                // ListProperty default value is empty list
                .ifEmpty { WebSubplatforms.entries.toList() }
            val enabledIosSubplatforms = definition.iosPlatform.subplatforms
                .map {
                    it.map { name ->
                        IosSubplatforms.entries.find { it.name.equals(name, ignoreCase = true) }
                            ?: throw GradleException(
                                "Unknown subplatform $name, accepted subplatforms: ${WebSubplatforms.entries.joinToString { it.name }}"
                            )
                    }
                }
                .getOrElse(IosSubplatforms.entries.toList())
                // ListProperty default value is empty list
                .ifEmpty { IosSubplatforms.entries.toList() }

            applyKotlinPlugin(enabledPlatforms, enabledWebSubplatforms, enabledIosSubplatforms)

            definition.dependencies.wireDependencies(enabledPlatforms)
            definition.testing.dependencies.wireTestingDependencies(enabledPlatforms)

            definition.wireKotlinCompilerOptions(
                context.objectFactory,
                enabledPlatforms,
                enabledWebSubplatforms,
                enabledIosSubplatforms,
            )

            if (buildModel.enabledPlatforms.get().contains(LibraryPlatforms.jvm)) {
                definition.configureJvmPlatform()
            }

            definition.testing.configureTesting(
                enabledPlatforms,
                enabledWebSubplatforms
            )

            definition.publishing.configurePublishing()
        }

        private fun applyKotlinPlugin(
            enabledPlatforms: Set<LibraryPlatforms>,
            enabledWebSubplatforms: List<WebSubplatforms>,
            enabledIosSubplatforms: List<IosSubplatforms>,
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
                                    if (enabledWebSubplatforms.contains(WebSubplatforms.js)) js { browser() }
                                    if (enabledWebSubplatforms.contains(WebSubplatforms.wasmJs)) wasmJs { browser() }
                                }

                                LibraryPlatforms.ios -> {
                                    if (enabledIosSubplatforms.contains(IosSubplatforms.iosArm64)) iosArm64()
                                    if (enabledIosSubplatforms.contains(IosSubplatforms.iosSimulatorArm64)) iosSimulatorArm64()
                                    if (enabledIosSubplatforms.contains(IosSubplatforms.iosX64)) iosX64()
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

        private fun LibraryTestingDependenciesExtension.wireTestingDependencies(
            enabledPlatforms: Set<LibraryPlatforms>
        ) {
            val configurations = this@LibraryApplyAction.project.configurations
            withJvmPlugin {
                @Suppress("UNCHECKED_CAST")
                val testCompilation = (target as KotlinWithJavaTarget<*, KotlinJvmCompilerOptions>).compilations
                    .getByName(KotlinCompilation.TEST_COMPILATION_NAME)

                val implementationConfiguration = configurations
                    .getByName(testCompilation.implementationConfigurationName)
                val compileOnlyConfiguration = configurations.getByName(testCompilation.compileOnlyConfigurationName)
                val runtimeOnlyConfiguration = configurations.getByName(testCompilation.runtimeOnlyConfigurationName)

                implementationConfiguration.fromDependencyCollector(implementation)
                implementationConfiguration.fromDependencyCollector(jvmPlatform.implementation)
                compileOnlyConfiguration.fromDependencyCollector(jvmPlatform.compileOnly)
                runtimeOnlyConfiguration.fromDependencyCollector(jvmPlatform.runtimeOnly)
            }

            withKmpPlugin {
                val commonSourceSet = sourceSets.getByName("commonTest")

                addDependencies(commonSourceSet.implementationConfigurationName, implementation)

                if (enabledPlatforms.contains(LibraryPlatforms.jvm)) {
                    val jvmTestSourceSet = sourceSets.getByName("jvmTest")
                    addDependencies(jvmTestSourceSet.implementationConfigurationName, jvmPlatform.implementation)
                    addDependencies(jvmTestSourceSet.compileOnlyConfigurationName, jvmPlatform.compileOnly)
                    addDependencies(jvmTestSourceSet.runtimeOnlyConfigurationName, jvmPlatform.runtimeOnly)
                }

                if (enabledPlatforms.contains(LibraryPlatforms.web)) {
                    val webTestSourceSet = sourceSets.getByName("webTest")

                    addDependencies(webTestSourceSet.implementationConfigurationName, webPlatform.implementation)
                }

                if (enabledPlatforms.contains(LibraryPlatforms.ios)) {
                    val iosTestSourceSet = sourceSets.getByName("iosTest")

                    addDependencies(iosTestSourceSet.implementationConfigurationName, iosPlatformDependencies.implementation)
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

                @Suppress("UNCHECKED_CAST")
                val testCompilation = (target as KotlinWithJavaTarget<*, KotlinJvmCompilerOptions>).compilations
                    .getByName(KotlinCompilation.TEST_COMPILATION_NAME)

                testCompilation.compileJavaTaskProvider.configure {
                    it.options.compilerArgumentProviders.add {
                        java.compilerOptions.compilerArgs.get()
                    }
                }

                project.tasks.named("test", Test::class.java) {
                    it.javaLauncher.convention(javaToolchainServices.launcherFor(
                        project.extensions.getByType(JavaPluginExtension::class.java).toolchain
                    ))
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

                val testCompilation = jvmTarget.compilations.getByName(KotlinCompilation.TEST_COMPILATION_NAME)

                testCompilation.compileJavaTaskProvider!!.configure {
                    it.options.compilerArgumentProviders.add {
                        java.compilerOptions.compilerArgs.get()
                    }
                }

                project.tasks.named("jvmTest", Test::class.java) {
                    it.javaLauncher.convention(javaToolchainServices.launcherFor(
                        project.extensions.getByType(JavaPluginExtension::class.java).toolchain
                    ))
                }
            }
        }

        private fun LibraryProjectType.wireKotlinCompilerOptions(
            objectFactory: ObjectFactory,
            enabledPlatforms: Set<LibraryPlatforms>,
            enabledWebSubplatforms: List<WebSubplatforms>,
            enabledIosSubplatforms: List<IosSubplatforms>,
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
                    val defaultJsOptions = objectFactory.newInstance(KotlinJsCompilerOptionsDefault::class.java)
                    syncKotlinCommonCompilerOptionsAsConvention(
                        compilerOptions,
                        defaultJsOptions,
                        defaultCommonOptions
                    )

                    enabledWebSubplatforms.forEach { subplatform ->
                        val target = when (subplatform) {
                            WebSubplatforms.js -> targets.getByName(subplatform.name) as KotlinJsTargetDsl
                            WebSubplatforms.wasmJs -> targets.getByName(subplatform.name) as KotlinWasmJsTargetDsl
                        }

                        syncKotlinJsCompilerOptionsAsConvention(
                            this@wireKotlinCompilerOptions.webPlatform.kotlin.compilerOptions,
                            target.compilerOptions,
                            defaultJsOptions
                        )
                    }
                }
                if (enabledPlatforms.contains(LibraryPlatforms.ios)) {
                    val defaultNativeOptions = objectFactory.newInstance(KotlinNativeCompilerOptionsDefault::class.java)
                    syncKotlinCommonCompilerOptionsAsConvention(
                        compilerOptions,
                        defaultNativeOptions,
                        defaultCommonOptions
                    )

                    enabledIosSubplatforms.forEach { subplatform ->
                        val target = targets.getByName(subplatform.name) as KotlinNativeTarget
                        syncKotlinNativeCompilerOptionsAsConvention(
                            this@wireKotlinCompilerOptions.iosPlatform.kotlin.compilerOptions,
                            target.compilerOptions,
                            defaultNativeOptions,
                        )
                    }
                }
            }
        }

        private fun LibraryTestingExtension.configureTesting(
            enabledPlatforms: Set<LibraryPlatforms>,
            enabledWebSubplatforms: List<WebSubplatforms>,
        ) {
            if (enabledPlatforms.contains(LibraryPlatforms.jvm)) {
                project.tasks.withType(Test::class.java).configureEach {
                    if (jvmPlatform.useJunitPlatform.getOrElse(false)) {
                        it.useJUnitPlatform()
                    }
                }
            }

            withKmpPlugin {
                if (enabledPlatforms.contains(LibraryPlatforms.web)) {
                    enabledWebSubplatforms.forEach {
                        val target = targets.getByName(it.name)
                        when (it) {
                            WebSubplatforms.wasmJs -> {
                                (target as KotlinWasmJsTargetDsl).browser {
                                    testTask {
                                        it.enabled = !webPlatform.skip.getOrElse(false)
                                    }
                                }
                            }
                            WebSubplatforms.js -> {
                                (target as KotlinJsTargetDsl).browser {
                                    testTask {
                                        it.enabled = !webPlatform.skip.getOrElse(false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun LibraryPublishingExtension.configurePublishing() {
            project.group = group.getOrElse(project.path.replace(":", "."))
            project.version = version.getOrElse(Project.DEFAULT_VERSION)
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