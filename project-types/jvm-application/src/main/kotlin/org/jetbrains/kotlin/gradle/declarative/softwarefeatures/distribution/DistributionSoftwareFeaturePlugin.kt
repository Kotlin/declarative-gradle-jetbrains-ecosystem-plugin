package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.distribution

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.jetbrains.kotlin.gradle.declarative.projecttypes.PackagingExtension
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(DistributionSoftwareFeaturePlugin.Binding::class)
public class DistributionSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectFeatureBinding {
        private val logger = Logging.getLogger(this::class.simpleName)

        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder.bindProjectFeature(
                "distribution",
                ProjectFeatureBindingBuilder.bindingToTargetDefinition(
                    DistributionDefinition::class.java,
                    PackagingExtension::class.java,
                )
            ) { context, definition, buildModel, _ ->
                logger.info("Applying distribution software feature")
                val services = context.objectFactory.newInstance(Services::class.java)


                services.pluginManager.apply("distribution-base")
                val distributionContainer = services.project.extensions.getByType(DistributionContainer::class.java)
                (buildModel as DefaultDistributionBuildModel)._distributions = distributionContainer

                buildModel.distributions.register(DistributionPlugin.MAIN_DISTRIBUTION_NAME) { distribution ->
                    distribution.distributionBaseName.value(definition.name.orElse("main"))
                    distribution.distributionClassifier.value(definition.classifier)
                }
            }.withUnsafeApplyAction()
                .withBuildModelImplementationType(DefaultDistributionBuildModel::class.java)
        }

        internal interface Services {
            // Unsafe service for apply action
            @get:Inject
            val pluginManager: PluginManager

            @get:Inject
            val project: Project
        }
    }
}