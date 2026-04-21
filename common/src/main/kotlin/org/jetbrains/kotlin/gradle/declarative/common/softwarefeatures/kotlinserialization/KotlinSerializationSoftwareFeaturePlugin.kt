package org.jetbrains.kotlin.gradle.declarative.common.softwarefeatures.kotlinserialization

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.common.definitions.KotlinJvmCompilationExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(KotlinSerializationSoftwareFeaturePlugin.Binding::class)
public class KotlinSerializationSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectFeatureBinding {

        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder
                .bindProjectFeature(
                    "serialization",
                    KotlinSerializationSoftwareFeatureApplyAction::class
                )
                .withUnsafeApplyAction()
        }
    }

    internal abstract class KotlinSerializationSoftwareFeatureApplyAction :
        ProjectFeatureApplyAction<KotlinSerializationDefinition, KotlinSerializationBuildModel, KotlinJvmCompilationExtension> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: KotlinSerializationDefinition,
            buildModel: KotlinSerializationBuildModel,
            parentDefinition: KotlinJvmCompilationExtension,
        ) {
            logger.info("Applying Kotlin Serialization software feature to project")

            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            buildModel.version.convention(definition.version.orElse(DEFAULT_SERIALIZATION_VERSION))
            buildModel.enabledFormats.convention(definition.enabledFormats.map { enabledFormats ->
                enabledFormats.map { entry ->
                    KotlinSerializationFormats.entries.singleOrNull {
                        it.name.equals(entry, ignoreCase = true)
                    } ?: throw IllegalArgumentException(
                        "Unknown Kotlin serialization format: $entry\n" +
                        "Available serialization formats: ${KotlinSerializationFormats.entries.map { it.name }}"
                    )
                }
            })

            // TODO: figure out how to access parent build model?
            // context.getBuildModel(parentDefinition) is not working here are build model there is BuildModel.None
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                val kotlinJvmExtension = project.extensions.getByType(KotlinJvmExtension::class.java)

                project.configurations.getByName(
                    kotlinJvmExtension.target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME).defaultSourceSet.apiConfigurationName
                ).dependencies.addAllLater(
                    buildModel.version.zip(buildModel.enabledFormats) { version, formats ->
                        formats.map { format ->
                            project.dependencies.create("org.jetbrains.kotlinx:kotlinx-serialization-${format.name.lowercase()}:$version")
                        }
                    }
                )
            }
        }
    }

    public companion object {
        public const val DEFAULT_SERIALIZATION_VERSION: String = "1.11.0"
    }
}