@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.jetbrains.kotlin.gradle.declarative.common.definitions.IosSubplatform
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinNativeCompilerOptionsAsConvention
import org.jetbrains.kotlin.gradle.declarative.xcode.registerXcodeProjectGenerationTasks
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectType(JetBrainsIosApplicationPlugin::class)
public class JetBrainsIosApplicationPlugin : Plugin<Project>, ProjectTypeBinding {

    override fun apply(target: Project): Unit = Unit

    override fun bind(builder: ProjectTypeBindingBuilder) {
        builder.bindProjectType("iosApplication", IosApplicationProjectTypeApplyAction::class)
            .withUnsafeDefinitionImplementationType(DefaultIosApplicationProjectType::class.java)
            .withUnsafeApplyAction()
    }

    internal abstract class IosApplicationProjectTypeApplyAction : ProjectTypeApplyAction<IosApplicationProjectType, IosApplicationBuildModel> {

        @get:Inject
        abstract val objectFactory: ObjectFactory

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val pluginManager: PluginManager

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: IosApplicationProjectType,
            buildModel: IosApplicationBuildModel
        ) {
            definition.applyKotlinMultiplatformPlugin(buildModel)
            definition.wireKotlinCompilerOptions(buildModel)
            definition.dependencies.wireDependencies(buildModel)
            definition.xcodeProject.registerXcodeProjectGenerationTasks()
        }

        private fun IosApplicationProjectType.applyKotlinMultiplatformPlugin(buildModel: IosApplicationBuildModel) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            withKmpPlugin {
                val enablesSubplatforms = subplatforms.getOrElse(IosSubplatform.entries).ifEmpty { IosSubplatform.entries }
                buildModel.enabledSubplatforms.value(enablesSubplatforms)
                enablesSubplatforms.forEach {
                    when (it) {
                        IosSubplatform.iosArm64 -> iosArm64()
                        IosSubplatform.iosSimulatorArm64 -> iosSimulatorArm64()
                        IosSubplatform.iosX64 -> iosX64()
                    }
                }
            }
        }

        private fun IosApplicationProjectType.wireKotlinCompilerOptions(buildModel: IosApplicationBuildModel) = withKmpPlugin {
            val enabledSubplatforms = buildModel.enabledSubplatforms.get()
            val defaultNativeOptions = objectFactory.newInstance(KotlinNativeCompilerOptionsDefault::class.java)

            enabledSubplatforms.forEach { subplatform ->
                val target = targets.getByName(subplatform.name) as KotlinNativeTarget
                syncKotlinNativeCompilerOptionsAsConvention(
                    this@wireKotlinCompilerOptions.kotlin.compilerOptions,
                    target.compilerOptions,
                    defaultNativeOptions,
                )
            }
        }

        private fun IosApplicationDependenciesExtension.wireDependencies(buildModel: IosApplicationBuildModel) = withKmpPlugin {
            // Wire dependencies to the main Gradle module
            val iosMainSourceSet = sourceSets.getByName("iosMain")
            addDependencies(iosMainSourceSet.apiConfigurationName, api)
            addDependencies(iosMainSourceSet.apiConfigurationName, exportApi)
            addDependencies(iosMainSourceSet.implementationConfigurationName, implementation)

            // Wire export dependencies to subplatform frameworks
            val enabledSubplatforms = buildModel.enabledSubplatforms.get()
            enabledSubplatforms.forEach { subplatform ->
                val target = targets.getByName(subplatform.name) as KotlinNativeTarget
                target.binaries.framework {
                    isStatic = true
                    addDependencies(exportConfigurationName, exportApi)
                }
            }
        }

        private fun withKmpPlugin(action: KotlinMultiplatformExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                project.extensions.getByType(KotlinMultiplatformExtension::class.java).apply(action)
            }
        }

        private fun addDependencies(name: String, dependencies: DependencyCollector) {
            project.configurations.getByName(name).fromDependencyCollector(dependencies)
        }
    }
}