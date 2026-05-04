package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.distribution

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.common.definitions.PackagingExtension
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(DistributionSoftwareFeaturePlugin.Binding::class)
public class DistributionSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectFeatureBinding {


        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder
                .bindProjectFeature(
                    "distribution",
                    DistributionSoftwareFeatureApplyAction::class,
                )
                .withUnsafeApplyAction()
                .withBuildModelImplementationType(DefaultDistributionBuildModel::class.java)
        }
    }

    internal abstract class DistributionSoftwareFeatureApplyAction : ProjectFeatureApplyAction<DistributionDefinition, DistributionBuildModel, PackagingExtension> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DistributionDefinition,
            buildModel: DistributionBuildModel,
            parentDefinition: PackagingExtension
        ) {
            logger.info("Applying distribution software feature")

            pluginManager.apply("distribution-base")
            val distributionContainer = project.extensions.getByType(DistributionContainer::class.java)
            (buildModel as DefaultDistributionBuildModel)._distributions = distributionContainer

            buildModel.distributions.register(DistributionPlugin.MAIN_DISTRIBUTION_NAME) { distribution ->
                distribution.distributionBaseName.value(definition.name.orElse("main"))
                distribution.distributionClassifier.value(definition.classifier)
            }
        }
    }
}