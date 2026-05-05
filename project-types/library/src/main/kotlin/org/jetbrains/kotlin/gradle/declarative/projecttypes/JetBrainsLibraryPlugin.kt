package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.GradleException
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
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectType(JetBrainsLibraryPlugin.Binding::class)
public class JetBrainsLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public abstract class Binding : ProjectTypeBinding {
        override fun bind(builder: ProjectTypeBindingBuilder) {
            builder
                .bindProjectType(
                    "library",
                    LibraryApplyAction::class
                )
                .withUnsafeApplyAction()
        }
    }

    @Suppress("OPT_IN_USAGE")
    internal abstract class LibraryApplyAction : ProjectTypeApplyAction<LibraryProjectType, LibraryBuildModel> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryProjectType,
            buildModel: LibraryBuildModel
        ) {
            logger.info("Applying 'library' project type")
            buildModel.enabledPlatforms.value(
                definition.platforms.map {
                    it.map { definitionValue ->
                        LibraryPlatforms.entries.find { it.name.equals(definitionValue, ignoreCase = true)  }
                            ?: throw GradleException(
                                "Platform '$definitionValue' is not available, available platforms:\n" +
                                        LibraryPlatforms.entries.joinToString { it.name }
                            )
                    }
                }
            )

            applyKotlinPlugin(buildModel.enabledPlatforms.get().toSet())
        }

        private fun applyKotlinPlugin(
            enabledPlatforms: Set<LibraryPlatforms>
        ) {
            when {
                enabledPlatforms.isEmpty() -> throw GradleException(
                    "No library platform was specified. Please specify at least one of ${LibraryPlatforms.entries.joinToString { it.name }}"
                )
                enabledPlatforms.size == 1 && enabledPlatforms == setOf(LibraryPlatforms.jvm) -> {
                    logger.info("Enabling Kotlin/JVM plugin")
                    pluginManager.apply("org.jetbrains.kotlin.jvm")
                }
                enabledPlatforms.size == 2 && enabledPlatforms == setOf(LibraryPlatforms.jvm, LibraryPlatforms.common) -> {
                    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
                    val multiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                    multiplatformExtension.jvm()
                    logger.info("Enabling Kotlin/KMP plugin with 'jvm()' target")
                }
                else -> {
                    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
                    val multiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                    enabledPlatforms.forEach { platform ->
                        when(platform) {
                            LibraryPlatforms.jvm -> multiplatformExtension.jvm()
                            LibraryPlatforms.common -> logger.warn("'common' platform is only used in conjunction with 'jvm' platform. Please remove it.")
                            LibraryPlatforms.web -> {
                                multiplatformExtension.js { browser() }
                                multiplatformExtension.wasmJs { browser() }
                            }
                            LibraryPlatforms.ios -> {
                                multiplatformExtension.iosArm64()
                                multiplatformExtension.iosSimulatorArm64()
                                multiplatformExtension.iosX64()
                            }
                        }
                    }
                }
            }
        }
    }
}