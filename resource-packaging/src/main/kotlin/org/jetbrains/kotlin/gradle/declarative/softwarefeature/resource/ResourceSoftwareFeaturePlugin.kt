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
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.declarative.common.definitions.PackagingExtension
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationDependenciesExtension
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

            builder
                .bindProjectFeature(
                    "resources",
                    ResourceBindingDependenciesApplyAction::class
                )
                .withUnsafeApplyAction()
                .withUnsafeDefinition()
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
                                ) {
                                    // FIXME: proper wire to execution distribution task
                                    it.builtBy(project.tasks.named("wasmJsBrowserDistribution"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    internal abstract class ResourceBindingDependenciesApplyAction :
            ProjectFeatureApplyAction<ResourceDependencyDefinition, BuildModel.None, JvmApplicationDependenciesExtension> {

        @get:Inject
        abstract val project: Project

        private val logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: ResourceDependencyDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmApplicationDependenciesExtension
        ) {
            logger.info("Applying resource packaging software feature")

            val depConf = project.configurations.dependencyScope("resources") {
                it.fromDependencyCollector(definition.resource)
            }
            val resConf = project.configurations.resolvable("resolvedResources") { configuration ->
                configuration.extendsFrom(depConf)
                configuration.attributes {attributeContainer ->
                    attributeContainer.attribute(
                        PackagingAttribute.attribute,
                        PackagingAttribute.RESOURCE,
                    )
                }
            }

            project.tasks.named("processResources", ProcessResources::class.java) {
                it.from(resConf.get()) {
                    it.into("static")
                }
            }
        }
    }
}