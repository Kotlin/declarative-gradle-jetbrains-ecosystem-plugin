package org.jetbrains.kotlin.gradle.declarative.softwarefeatures

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.projecttypes.LibraryPublishingExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URI
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectFeature(MavenPublishSoftwareFeaturePlugin.Binding::class)
public class MavenPublishSoftwareFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectFeatureBinding {
        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder
                .bindProjectFeature("maven", MavenPublishApplyAction::class)
                .withUnsafeApplyAction()

        }
    }

    internal abstract class MavenPublishApplyAction :
        ProjectFeatureApplyAction<MavenPublishDefinition, BuildModel.None, LibraryPublishingExtension> {

        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: MavenPublishDefinition,
            buildModel: BuildModel.None,
            parentDefinition: LibraryPublishingExtension
        ) {
            pluginManager.apply("maven-publish")
            val publishingExtension = project.extensions.getByType(PublishingExtension::class.java)

            publishingExtension.repositories.maven {
                it.name = definition.name.get()
                it.url = URI.create(definition.repositoryUrl.get())
            }

            withJvmPlugin {
                publishingExtension.publications.register(definition.name.get(), MavenPublication::class.java) {
                    it.from(project.components.getByName("java"))
                }

                project.extensions.configure(JavaPluginExtension::class.java) {
                    if (definition.withDocs.getOrElse(false)) {
                        it.withJavadocJar()
                    }
                    if (definition.withSources.getOrElse(false)) {
                        it.withSourcesJar()
                    }
                }
            }

            withKmpPlugin {
                withSourcesJar(definition.withSources.getOrElse(false))
            }
        }

        private fun withJvmPlugin(action: KotlinJvmExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                val kotlinJvmExtension = project.extensions.getByType(KotlinJvmExtension::class.java)
                action(kotlinJvmExtension)
            }
        }

        private fun withKmpPlugin(action: KotlinMultiplatformExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                action(kmpExtension)
            }
        }
    }
}