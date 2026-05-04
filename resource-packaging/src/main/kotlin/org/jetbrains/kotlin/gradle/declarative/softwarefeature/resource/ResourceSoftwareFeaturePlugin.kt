package org.jetbrains.kotlin.gradle.declarative.softwarefeature.resource

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.common.definitions.PackagingExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.Executable
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(ResourceSoftwareFeaturePlugin.ResourceBinding::class)
public abstract class ResourceSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    internal class ResourceBinding : ProjectFeatureBinding {
        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder
                .bindProjectFeature(
                    "resource",
                    ResourceBindingPackagingApplyAction::class
                )
                .withUnsafeApplyAction()
        }
    }

    public object PackagingAttribute {
        public val attribute: Attribute<String> = Attribute.of(NAME, String::class.java)
        public const val NAME: String = "packaging"
        public const val RESOURCE: String = "resource"
    }

    internal abstract class ResourceBindingPackagingApplyAction :
        ProjectFeatureApplyAction<PackageAsResourceDefinition, BuildModel.None, PackagingExtension> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: PackageAsResourceDefinition,
            buildModel: BuildModel.None,
            parentDefinition: PackagingExtension
        ) {
            logger.info("Applying resource packaging software feature")

            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val multiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

                val resourcesConfiguration = project.configurations.consumable("packaged-as-resources") { configuration ->
                    configuration.attributes { attributeContainer ->
                        attributeContainer.attribute(
                            PackagingAttribute.attribute,
                            PackagingAttribute.RESOURCE,
                        )
                    }
                }

                multiplatformExtension.targets.withType(KotlinWasmJsTargetDsl::class.java).configureEach {
                    it.binaries.withType(Executable::class.java).configureEach { executable ->
                        if (executable.mode == KotlinJsBinaryMode.PRODUCTION) {
                            project.artifacts {
                                it.add(
                                resourcesConfiguration.name,
                                    executable.distribution.outputDirectory
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}