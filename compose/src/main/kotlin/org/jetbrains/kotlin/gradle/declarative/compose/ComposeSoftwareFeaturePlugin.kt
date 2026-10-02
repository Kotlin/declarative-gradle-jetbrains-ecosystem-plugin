package org.jetbrains.kotlin.gradle.declarative.compose

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(ComposeSoftwareFeaturePlugin.Binding::class)
public class ComposeSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = Unit

    public class Binding : ProjectFeatureBinding {
        override fun bind(builder: ProjectFeatureBindingBuilder) {
            TODO("Not yet implemented")
        }
    }

    internal sealed class ComposeSoftwareFeatureApplyAction<D : Definition<*>> : ProjectFeatureApplyAction<ComposeDefinition, ComposeBuildModel, D> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: ComposeDefinition,
            buildModel: ComposeBuildModel,
            parentDefinition: D
        ) {
            logger.info("Applying Compose software feature to the project.")

            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        }
    }
}