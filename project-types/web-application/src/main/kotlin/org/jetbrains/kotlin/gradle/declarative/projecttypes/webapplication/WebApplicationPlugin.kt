package org.jetbrains.kotlin.gradle.declarative.projecttypes.webapplication

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectType(WebApplicationPlugin.Binding::class)
public class WebApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectTypeBinding {
        override fun bind(builder: ProjectTypeBindingBuilder) {
            builder
                .bindProjectType("webApplication", WebApplicationApplyAction::class)
                .withUnsafeApplyAction()
                .withUnsafeDefinition()
        }
    }

    internal abstract class WebApplicationApplyAction :
            ProjectTypeApplyAction<WebApplicationProjectType, WebApplicationBuildModel> {

        // Unsafe service for apply action
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(WebApplicationPlugin::class.qualifiedName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: WebApplicationProjectType,
            buildModel: WebApplicationBuildModel
        ) {
            logger.info("Applying JetBrains Web Application plugin to ${project.path}")

            pluginManager.apply("org.jetbrains.kotlin.multiplatform")

            val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kmpExtension.js {
                browser()
                binaries.executable()
            }
            @OptIn(ExperimentalWasmDsl::class)
            kmpExtension.wasmJs {
                browser()
                binaries.executable()
            }
            kmpExtension.applyDefaultHierarchyTemplate()

            val webMainSourceSet = kmpExtension.sourceSets.getByName("webMain")

            definition.dependencies.implementation.dependencies.getOrElse(emptySet()).forEach { dependency ->
                webMainSourceSet.dependencies { implementation(dependency) }
            }
        }
    }
}