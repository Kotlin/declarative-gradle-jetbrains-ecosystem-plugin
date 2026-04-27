package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.spring

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
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import org.springframework.boot.gradle.dsl.SpringBootExtension
import javax.inject.Inject
import kotlin.jvm.java

@Suppress("UnstableApiUsage")
@BindsProjectFeature(SpringSoftwareFeaturePlugin.Binding::class)
public class SpringSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectFeatureBinding {
        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder
                .bindProjectFeature(
                    "spring",
                    SpringSoftwareFeatureApplyAction::class
                )
                .withUnsafeApplyAction()
        }
    }

    internal abstract class SpringSoftwareFeatureApplyAction :
        ProjectFeatureApplyAction<SpringDefinition, SpringBuildModel, JvmApplicationProjectType> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SpringDefinition,
            buildModel: SpringBuildModel,
            parentDefinition: JvmApplicationProjectType,
        ) {
            logger.info("Applying Spring software feature to project")

            pluginManager.apply("org.springframework.boot")
            pluginManager.apply("org.jetbrains.kotlin.plugin.spring")

            val springExtension = project.extensions.getByType(SpringBootExtension::class.java)
            val jvmApplicationBuildModel = context.getBuildModel(parentDefinition)
            springExtension.mainClass.set(jvmApplicationBuildModel.applications.getByName("main").mainClassName)
        }
    }
}