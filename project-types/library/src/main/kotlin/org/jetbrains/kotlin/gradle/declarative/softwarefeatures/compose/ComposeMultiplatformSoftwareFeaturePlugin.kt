package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.compose

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
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryProjectType
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(ComposeMultiplatformSoftwareFeaturePlugin::class)
public class ComposeMultiplatformSoftwareFeaturePlugin : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project): Unit = Unit

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("compose", ComposeLibrarySoftwareFeatureApplyAction::class)
            .withBuildModelImplementationType(DefaultComposeBuildModel::class.java)
            .withUnsafeApplyAction()
    }

    internal abstract class ComposeLibrarySoftwareFeatureApplyAction : ProjectFeatureApplyAction<ComposeDefinition, ComposeBuildModel, LibraryProjectType> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: ComposeDefinition,
            buildModel: ComposeBuildModel,
            parentDefinition: LibraryProjectType
        ) {
            logger.info("Applying Compose Multiplatform software feature to the library project.")

            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        }

        @Suppress("unused")
        private inline fun withComposeMultiplatform(crossinline action: ComposeExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.compose") {
                project.extensions.getByType(ComposeExtension::class.java).action()
            }
        }
    }
}