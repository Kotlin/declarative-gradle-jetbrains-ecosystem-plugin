@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative.projectfeature.androidecosystem

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.internal.GradleInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.populateSpec
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.android.whenAGPAvailable
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinCommonCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJvmCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryDefinition
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryDependenciesExtension
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryPlatform
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryProjectType
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryTestingDependenciesExtension
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryTestingExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.DefaultKotlinJavaToolchain
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(AndroidEcosystemProjectFeaturePlugin::class)
public class AndroidEcosystemProjectFeaturePlugin : Plugin<Project>, ProjectFeatureBinding {

    override fun apply(target: Project): Unit = Unit

    override fun bind(builder: ProjectFeatureBindingBuilder): Unit = whenAGPAvailable {
        builder.bindProjectFeature("androidPlatform", AndroidEcosystemApplyAction::class)
            .withUnsafeDefinitionImplementationType(DefaultLibraryAndroidEcosystemDefinition::class.java)
            .withBuildModelImplementationType(DefaultLibraryAndroidBuildModel::class.java)
            .withUnsafeApplyAction()
        builder.bindProjectFeature("androidPlatform", AndroidPlatformDependenciesApplyAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
        builder.bindProjectFeature("androidPlatform", TestingAndroidPlatformDependenciesApplyAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
        builder.bindProjectFeature("androidPlatform", TestingAndroidPlatformApplyAction::class)
            .withUnsafeDefinitionImplementationType(DefaultLibraryTestingAndroidEcosystemDefinition::class.java)
            .withUnsafeApplyAction()
    }

    internal abstract class AndroidPlatformFeatureApplyAction<D : Definition<B>, B : BuildModel, P : LibraryDefinition> : ProjectFeatureApplyAction<D, B, P> {

        @get:Inject
        protected abstract val project: Project

        @get:Inject
        protected abstract val pluginManager: PluginManager

        private val hasAndroidEcosystemPlugin = (project.gradle as GradleInternal).settings.plugins.hasPlugin("com.android.ecosystem")

        context(context: ProjectFeatureApplicationContext)
        protected fun LibraryDefinition.withAndroidPlatform(action: () -> Unit) {
            val buildModel = context.getBuildModel(this)
            if (hasAndroidEcosystemPlugin && LibraryPlatform.android in buildModel.enabledPlatforms.get()) {
                action()
            }
        }

        protected fun withKmpPlugin(action: KotlinMultiplatformExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                action(kmpExtension)
            }
        }

        protected inline fun KotlinMultiplatformExtension.android(crossinline action: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = {}) {
            pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
                extensions.getByType(KotlinMultiplatformAndroidLibraryTarget::class.java).action()
            }
        }

        protected fun addDependencies(name: String, dependencies: DependencyCollector) {
            project.configurations.getByName(name).fromDependencyCollector(dependencies)
        }
    }

    internal abstract class AndroidEcosystemApplyAction : AndroidPlatformFeatureApplyAction<LibraryAndroidEcosystemDefinition, LibraryAndroidBuildModel, LibraryProjectType>() {

        @get:Inject
        abstract val objectFactory: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryAndroidEcosystemDefinition,
            buildModel: LibraryAndroidBuildModel,
            parentDefinition: LibraryProjectType
        ) {
            context(context) {
                parentDefinition.withAndroidPlatform {
                    withKmpPlugin {
                        android {
                            definition.minSdk.orNull?.let { version ->
                                minSdk { version.populateSpec() }
                            }
                            definition.compileSdk.orNull?.let { version ->
                                compileSdk { version.populateSpec() }
                            }
                            definition.namespace.orNull?.let { namespace = it }

                            val commonCompilerOptions = this@withKmpPlugin.compilerOptions
                            val androidJvmCompilerOptions = this@android.compilerOptions
                            val defaultAndroidJvmOptions = objectFactory.newInstance(KotlinJvmCompilerOptionsDefault::class.java)

                            syncKotlinCommonCompilerOptionsAsConvention(
                                commonCompilerOptions,
                                defaultAndroidJvmOptions,
                                commonCompilerOptions
                            )
                            syncKotlinJvmCompilerOptionsAsConvention(
                                definition.kotlin.compilerOptions,
                                androidJvmCompilerOptions,
                                defaultAndroidJvmOptions
                            )
                            DefaultKotlinJavaToolchain.wireJvmTargetToToolchain(androidJvmCompilerOptions, project)
                        }
                    }
                }
            }
        }
    }

    internal abstract class AndroidPlatformDependenciesApplyAction : AndroidPlatformFeatureApplyAction<LibraryAndroidPlatformDependencies, BuildModel.None, LibraryDependenciesExtension>() {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryAndroidPlatformDependencies,
            buildModel: BuildModel.None,
            parentDefinition: LibraryDependenciesExtension
        ) {
            context(context) {
                parentDefinition.withAndroidPlatform {
                    withKmpPlugin {
                        val androidMainSourceSet = sourceSets.getByName("androidMain")
                        addDependencies(androidMainSourceSet.apiConfigurationName, definition.api)
                        addDependencies(androidMainSourceSet.implementationConfigurationName, definition.implementation)
                        addDependencies(androidMainSourceSet.compileOnlyConfigurationName, definition.compileOnly)
                        addDependencies(androidMainSourceSet.runtimeOnlyConfigurationName, definition.runtimeOnly)
                        addDependencies("androidRuntimeClasspath", definition.runtimeClasspath)
                    }
                }
            }
        }
    }

    internal abstract class TestingAndroidPlatformDependenciesApplyAction : AndroidPlatformFeatureApplyAction<LibraryTestingAndroidPlatformDependencies, BuildModel.None, LibraryTestingDependenciesExtension>() {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryTestingAndroidPlatformDependencies,
            buildModel: BuildModel.None,
            parentDefinition: LibraryTestingDependenciesExtension
        ) {
            context(context) {
                parentDefinition.withAndroidPlatform {
                    withKmpPlugin {
                        // Test source sets are configured given the `androidPlatform` containing either the `hostTest` and/or `deviceTest` extensions
                        sourceSets.findByName("androidHostTest")?.let { sourceSet ->
                            addDependencies(sourceSet.implementationConfigurationName, definition.hostTest.implementation)
                            addDependencies(sourceSet.compileOnlyConfigurationName, definition.hostTest.compileOnly)
                            addDependencies(sourceSet.runtimeOnlyConfigurationName, definition.hostTest.runtimeOnly)
                        }
                        sourceSets.findByName("androidDeviceTest")?.let { sourceSet ->
                            addDependencies(sourceSet.implementationConfigurationName, definition.deviceTest.implementation)
                            addDependencies(sourceSet.compileOnlyConfigurationName, definition.deviceTest.compileOnly)
                            addDependencies(sourceSet.runtimeOnlyConfigurationName, definition.deviceTest.runtimeOnly)
                        }
                    }
                }
            }
        }
    }

    internal abstract class TestingAndroidPlatformApplyAction : AndroidPlatformFeatureApplyAction<LibraryTestingAndroidEcosystemDefinition, BuildModel.None, LibraryTestingExtension>() {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryTestingAndroidEcosystemDefinition,
            buildModel: BuildModel.None,
            parentDefinition: LibraryTestingExtension
        ) {
            withKmpPlugin {
                android {
                    withHostTest {
                        definition.hostTest.targetSdk.orNull?.let { version ->
                            targetSdk { version.populateSpec() }
                        }
                        definition.hostTest.returnDefaultValues.orNull?.let { isReturnDefaultValues = it }
                        definition.hostTest.enableCoverage.orNull?.let { enableCoverage = it }
                    }
                    withDeviceTest {
                        definition.deviceTest.targetSdk.orNull?.let { version ->
                            targetSdk { version.populateSpec() }
                        }
                        definition.deviceTest.applicationId.orNull?.let { applicationId = it }
                        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                        definition.deviceTest.handleProfiling.orNull?.let { handleProfiling = it }
                        definition.deviceTest.functionalTest.orNull?.let { functionalTest = it }
                        definition.deviceTest.enableCoverage.orNull?.let { enableCoverage = it }
                    }
                }
            }
        }
    }
}