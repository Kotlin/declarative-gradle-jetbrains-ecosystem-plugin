@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative.projecttypes

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.internal.GradleInternal
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
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
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.EcosystemWithSubplatforms
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.ios.IosSubplatform
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.Subplatform
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.web.WebSubplatform
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinCommonCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJvmCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJsCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinNativeCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.util.configureAndroidPlatform
import org.jetbrains.kotlin.gradle.declarative.util.configureAndroidTesting
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
import kotlin.enums.EnumEntries
import kotlin.jvm.java

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
                .withUnsafeDefinitionImplementationType(DefaultLibraryProjectType::class.java)
                .withUnsafeApplyAction()
        }
    }

    internal object LibraryProblems {
        private val GROUP: ProblemGroup = ProblemGroup.create("org.jetbrains.kotlin.gradle.declarative.JetBrainsLibraryPlugin", "JetBrains Library Project Problems")

        val MISSING_ANDROID_ECOSYSTEM_PLUGIN = ProblemId.create("missing-android-ecosystem-plugin", "Missing Android ecosystem plugin in KMP library with 'android' platform", GROUP)
    }

    @Suppress("OPT_IN_USAGE")
    internal abstract class LibraryApplyAction : ProjectTypeApplyAction<LibraryProjectType, LibraryBuildModel> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val javaToolchainServices: JavaToolchainService

        @get:Inject
        abstract val objectFactory: ObjectFactory

        @get:Inject
        abstract val problems: Problems

        private val logger = Logging.getLogger(this::class.simpleName)

        private val hasAndroidEcosystemPlugin = (project.gradle as GradleInternal).settings.plugins.hasPlugin("com.android.ecosystem")

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryProjectType,
            buildModel: LibraryBuildModel
        ) {
            logger.info("Applying 'library' project type")

            val enabledPlatforms = definition.platforms
            buildModel.enabledPlatforms.value(enabledPlatforms)

            val enabledWebSubplatforms = buildModel.collectSubplatforms(LibraryPlatform.web, WebSubplatform.entries, definition.webPlatform)
            buildModel.enabledWebSubplatforms.value(enabledWebSubplatforms)

            val enabledIosSubplatforms = buildModel.collectSubplatforms(LibraryPlatform.ios, IosSubplatform.entries, definition.iosPlatform)
            buildModel.enabledIosSubplatforms.value(enabledIosSubplatforms)

            applyKotlinPlugin(buildModel)

            if (hasAndroidEcosystemPlugin && LibraryPlatform.android in buildModel.enabledPlatforms.get()) {
                withKmpPlugin {
                    android { definition.androidPlatform.configureAndroidPlatform() }
                }
            }

            definition.dependencies.wireDependencies(buildModel)
            definition.testing.dependencies.wireTestingDependencies(buildModel)

            definition.wireKotlinCompilerOptions(buildModel)

            if (LibraryPlatform.jvm in buildModel.enabledPlatforms.get()) {
                definition.configureJvmPlatform()
            }

            definition.testing.configureTesting(buildModel)

            context(context) {
                definition.publishing.configurePublishing(buildModel)
            }
        }

        private fun <S> LibraryBuildModel.collectSubplatforms(
            platform: LibraryPlatform,
            allSubplatforms: EnumEntries<S>,
            definition: EcosystemWithSubplatforms<S>
        ): List<S> where S : Subplatform, S : Enum<S> = when {
            platform in enabledPlatforms.get() ->
                definition.subplatforms
                    .getOrElse(allSubplatforms.toList())
                    // ListProperty default value is empty list
                    .ifEmpty { allSubplatforms.toList() }
            else -> emptyList()
        }

        private fun applyKotlinPlugin(buildModel: LibraryBuildModel) {
            val enabledPlatforms = buildModel.enabledPlatforms.get()
            val enabledWebSubplatforms = buildModel.enabledWebSubplatforms.get()
            val enabledIosSubplatforms = buildModel.enabledIosSubplatforms.get()
            when {
                enabledPlatforms.isEmpty() -> throw GradleException(
                    "No library platform was specified. Please specify at least one of ${LibraryPlatform.entries.joinToString { it.name }}"
                )
                enabledPlatforms.size == 1 && enabledPlatforms == setOf(LibraryPlatform.jvm) -> {
                    logger.info("Enabling Kotlin/JVM plugin")
                    pluginManager.apply("org.jetbrains.kotlin.jvm")
                }
                enabledPlatforms.size == 2 && enabledPlatforms == setOf(LibraryPlatform.jvm, LibraryPlatform.common) -> {
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
                                LibraryPlatform.jvm -> jvm()
                                LibraryPlatform.common -> logger.warn("'common' platform is only used in conjunction with 'jvm' platform. Please remove it.")
                                LibraryPlatform.web -> {
                                    if (WebSubplatform.js in enabledWebSubplatforms) js { browser() }
                                    if (WebSubplatform.wasmJs in enabledWebSubplatforms) wasmJs { browser() }
                                }

                                LibraryPlatform.ios -> {
                                    if (IosSubplatform.iosArm64 in enabledIosSubplatforms) iosArm64()
                                    if (IosSubplatform.iosSimulatorArm64 in enabledIosSubplatforms) iosSimulatorArm64()
                                    if (IosSubplatform.iosX64 in enabledIosSubplatforms) iosX64()
                                }
                                LibraryPlatform.android if hasAndroidEcosystemPlugin -> pluginManager.apply("com.android.kotlin.multiplatform.library")
                                LibraryPlatform.android -> throw problems.reporter.throwing(
                                    GradleException("Android ecosystem plugin was not applied"),
                                    LibraryProblems.MISSING_ANDROID_ECOSYSTEM_PLUGIN
                                ) {
                                    it.details("The 'android' library platform can only used when Android ecosystem plugin is applied to 'settings.gradle.dcl'")
                                        .solution("Apply the plugin with 'id(\"com.android.ecosystem\").version(...)' in 'settings.gradle.dcl'")
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun LibraryDependenciesExtension.wireDependencies(buildModel: LibraryBuildModel) {
            val enabledPlatforms = buildModel.enabledPlatforms.get()
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

                if (LibraryPlatform.jvm in enabledPlatforms) {
                    val jvmMainSourceSet = sourceSets.getByName("jvmMain")
                    addDependencies(jvmMainSourceSet.apiConfigurationName, jvmPlatform.api)
                    addDependencies(jvmMainSourceSet.implementationConfigurationName, jvmPlatform.implementation)
                    addDependencies(jvmMainSourceSet.compileOnlyConfigurationName, jvmPlatform.compileOnly)
                    addDependencies(jvmMainSourceSet.runtimeOnlyConfigurationName, jvmPlatform.runtimeOnly)
                }

                if (LibraryPlatform.web in enabledPlatforms) {
                    val webMainSourceSet = sourceSets.getByName("webMain")

                    addDependencies(webMainSourceSet.apiConfigurationName, webPlatform.api)
                    addDependencies(webMainSourceSet.implementationConfigurationName, webPlatform.implementation)
                }

                if (LibraryPlatform.ios in enabledPlatforms) {
                    val iosMainSourceSet = sourceSets.getByName("iosMain")

                    addDependencies(iosMainSourceSet.apiConfigurationName, iosPlatform.api)
                    addDependencies(iosMainSourceSet.implementationConfigurationName, iosPlatform.implementation)
                }

                if (hasAndroidEcosystemPlugin && LibraryPlatform.android in enabledPlatforms) {
                    val androidMainSourceSet = sourceSets.getByName("androidMain")
                    addDependencies(androidMainSourceSet.apiConfigurationName, androidPlatform.api)
                    addDependencies(androidMainSourceSet.implementationConfigurationName, androidPlatform.implementation)
                    addDependencies(androidMainSourceSet.compileOnlyConfigurationName, androidPlatform.compileOnly)
                    addDependencies(androidMainSourceSet.runtimeOnlyConfigurationName, androidPlatform.runtimeOnly)
                    addDependencies("androidRuntimeClasspath", androidPlatform.runtimeClasspath)
                }
            }
        }

        private fun LibraryTestingDependenciesExtension.wireTestingDependencies(buildModel: LibraryBuildModel) {
            val enabledPlatforms = buildModel.enabledPlatforms.get()
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

                if (LibraryPlatform.jvm in enabledPlatforms) {
                    val jvmTestSourceSet = sourceSets.getByName("jvmTest")
                    addDependencies(jvmTestSourceSet.implementationConfigurationName, jvmPlatform.implementation)
                    addDependencies(jvmTestSourceSet.compileOnlyConfigurationName, jvmPlatform.compileOnly)
                    addDependencies(jvmTestSourceSet.runtimeOnlyConfigurationName, jvmPlatform.runtimeOnly)
                }

                if (LibraryPlatform.web in enabledPlatforms) {
                    val webTestSourceSet = sourceSets.getByName("webTest")

                    addDependencies(webTestSourceSet.implementationConfigurationName, webPlatform.implementation)
                }

                if (LibraryPlatform.ios in enabledPlatforms) {
                    val iosTestSourceSet = sourceSets.getByName("iosTest")

                    addDependencies(iosTestSourceSet.implementationConfigurationName, iosPlatform.implementation)
                }

                if (hasAndroidEcosystemPlugin && LibraryPlatform.android in enabledPlatforms) {
                    // Test source sets are configured given the `androidPlatform` containing either the `hostTest` and/or `deviceTest` extensions
                    sourceSets.findByName("androidHostTest")?.let { androidHostTestSourceSet ->
                        addDependencies(androidHostTestSourceSet.implementationConfigurationName, androidPlatform.hostTest.implementation)
                        addDependencies(androidHostTestSourceSet.compileOnlyConfigurationName, androidPlatform.hostTest.compileOnly)
                        addDependencies(androidHostTestSourceSet.runtimeOnlyConfigurationName, androidPlatform.hostTest.runtimeOnly)
                    }
                    sourceSets.findByName("androidDeviceTest")?.let { androidDeviceTestSourceSet ->
                        addDependencies(androidDeviceTestSourceSet.implementationConfigurationName, androidPlatform.deviceTest.implementation)
                        addDependencies(androidDeviceTestSourceSet.compileOnlyConfigurationName, androidPlatform.deviceTest.compileOnly)
                        addDependencies(androidDeviceTestSourceSet.runtimeOnlyConfigurationName, androidPlatform.deviceTest.runtimeOnly)
                    }
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

        private fun LibraryProjectType.wireKotlinCompilerOptions(buildModel: LibraryBuildModel) {
            val enabledPlatforms = buildModel.enabledPlatforms.get()
            val enabledWebSubplatforms = buildModel.enabledWebSubplatforms.get()
            val enabledIosSubplatforms = buildModel.enabledIosSubplatforms.get()
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

                if (LibraryPlatform.jvm in enabledPlatforms) {
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
                if (LibraryPlatform.web in enabledPlatforms) {
                    val defaultJsOptions = objectFactory.newInstance(KotlinJsCompilerOptionsDefault::class.java)
                    syncKotlinCommonCompilerOptionsAsConvention(
                        compilerOptions,
                        defaultJsOptions,
                        defaultCommonOptions
                    )

                    enabledWebSubplatforms.forEach { subplatform ->
                        val target = when (subplatform) {
                            WebSubplatform.js -> targets.getByName(subplatform.name) as KotlinJsTargetDsl
                            WebSubplatform.wasmJs -> targets.getByName(subplatform.name) as KotlinWasmJsTargetDsl
                        }

                        syncKotlinJsCompilerOptionsAsConvention(
                            this@wireKotlinCompilerOptions.webPlatform.kotlin.compilerOptions,
                            target.compilerOptions,
                            defaultJsOptions
                        )
                    }
                }
                if (LibraryPlatform.ios in enabledPlatforms) {
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

                if (hasAndroidEcosystemPlugin && LibraryPlatform.android in enabledPlatforms) {
                    android {
                        val defaultAndroidJvmOptions = objectFactory.newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                        syncKotlinCommonCompilerOptionsAsConvention(
                            this@withKmpPlugin.compilerOptions,
                            defaultAndroidJvmOptions,
                            defaultCommonOptions
                        )
                        syncKotlinJvmCompilerOptionsAsConvention(
                            this@wireKotlinCompilerOptions.androidPlatform.kotlin.compilerOptions,
                            compilerOptions,
                            defaultAndroidJvmOptions
                        )
                        DefaultKotlinJavaToolchain.wireJvmTargetToToolchain(compilerOptions, project)
                    }
                }
            }
        }

        private fun LibraryTestingExtension.configureTesting(buildModel: LibraryBuildModel) {
            val enabledPlatforms = buildModel.enabledPlatforms.get()
            val enabledWebSubplatforms = buildModel.enabledWebSubplatforms.get()

            if (LibraryPlatform.jvm in enabledPlatforms) {
                project.tasks.withType(Test::class.java).configureEach {
                    if (jvmPlatform.useJUnitPlatform.getOrElse(false)) {
                        it.useJUnitPlatform()
                    }
                }
            }

            withKmpPlugin {
                if (LibraryPlatform.web in enabledPlatforms) {
                    enabledWebSubplatforms.forEach {
                        val target = targets.getByName(it.name)
                        when (it) {
                            WebSubplatform.wasmJs -> {
                                (target as KotlinWasmJsTargetDsl).browser {
                                    testTask {
                                        it.enabled = !webPlatform.skip.getOrElse(false)
                                    }
                                }
                            }
                            WebSubplatform.js -> {
                                (target as KotlinJsTargetDsl).browser {
                                    testTask {
                                        it.enabled = !webPlatform.skip.getOrElse(false)
                                    }
                                }
                            }
                        }
                    }
                }

                if (hasAndroidEcosystemPlugin && LibraryPlatform.android in enabledPlatforms) {
                    android { androidPlatform.configureAndroidTesting() }
                }
            }
        }

        context(context: ProjectFeatureApplicationContext)
        private fun LibraryPublishingExtension.configurePublishing(buildModel: LibraryBuildModel) {
            val publishingBuildModel = context.getBuildModel(this)
            // Sync the build models as they are different instances under different definitions
            publishingBuildModel.enabledPlatforms.value(buildModel.enabledPlatforms)
            publishingBuildModel.enabledWebSubplatforms.value(buildModel.enabledWebSubplatforms)
            publishingBuildModel.enabledIosSubplatforms.value(buildModel.enabledIosSubplatforms)

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

        private inline fun KotlinMultiplatformExtension.android(crossinline action: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = {}) {
            pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
                extensions.getByType(KotlinMultiplatformAndroidLibraryTarget::class.java).action()
            }
        }

        private fun addDependencies(name: String, dependencies: DependencyCollector) {
            project.configurations.getByName(name).fromDependencyCollector(dependencies)
        }
    }
}