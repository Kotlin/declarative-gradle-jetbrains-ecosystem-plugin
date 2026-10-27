package org.jetbrains.kotlin.gradle.declarative.projectfeature.iosframework

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.declarative.projecttype.LibraryPublishingExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(IosFrameworkPublishingProjectFeaturePlugin::class)
public class IosFrameworkPublishingProjectFeaturePlugin : Plugin<Project>, ProjectFeatureBinding {

    override fun apply(target: Project): Unit = Unit

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("iosFramework", IosFrameworkPublishingApplyAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    internal abstract class IosFrameworkPublishingApplyAction : ProjectFeatureApplyAction<IosFrameworkPublishingDefinition, BuildModel.None, LibraryPublishingExtension> {

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val pluginManager: PluginManager

        private val logger = Logging.getLogger(this::class.simpleName)

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: IosFrameworkPublishingDefinition,
            buildModel: BuildModel.None,
            parentDefinition: LibraryPublishingExtension
        ) {
            val buildModel = context.getBuildModel(parentDefinition)
            val enabledIosSubplatforms = buildModel.enabledIosSubplatforms.get()
            if (enabledIosSubplatforms.isEmpty()) return

            logger.info("Configuring publishing as framework for iOS subplatforms: $enabledIosSubplatforms")

            withKmpPlugin {
                enabledIosSubplatforms.forEach { subplatform ->
                    val target = targets.getByName(subplatform.name) as KotlinNativeTarget
                    target.binaries.framework {
                        definition.static.orNull?.let { isStatic = it }
                        definition.linkerOptions.orNull?.let { linkerOpts = it }
                        freeCompilerArgs = target.compilerOptions.freeCompilerArgs.getOrElse(emptyList())
                        project.configurations.getByName(exportConfigurationName).fromDependencyCollector(definition.export)
                        definition.transitiveExport.orNull?.let { transitiveExport = it }
                    }
                }
            }
        }

        private fun withKmpPlugin(action: KotlinMultiplatformExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                action(kmpExtension)
            }
        }
    }
}