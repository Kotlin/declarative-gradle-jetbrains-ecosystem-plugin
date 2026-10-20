package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.compose

import com.android.build.api.dsl.ApplicationDeclarativeDefinition
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.Definition
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

    private val logger: Logger = Logging.getLogger(this::class.simpleName)

    override fun apply(target: Project): Unit = Unit

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("compose", ComposeLibrarySoftwareFeatureApplyAction::class)
            .withBuildModelImplementationType(DefaultComposeBuildModel::class.java)
            .withUnsafeApplyAction()

        // TODO: figure out how to neatly check settings' plugins during binding
        try {
            builder.bindProjectFeature("compose", ComposeAndroidApplicationSoftwareFeatureApplyAction::class)
                .withBuildModelImplementationType(DefaultComposeBuildModel::class.java)
                .withUnsafeApplyAction()
            logger.info("Android Application found, and Compose feature bound")
        } catch (_: NoClassDefFoundError) {

        }
    }

    internal abstract class ComposeSoftwareFeatureApplyAction<D : ComposeDefinition, P : Definition<*>> : ProjectFeatureApplyAction<D, ComposeBuildModel, P> {

        @get:Inject
        protected abstract val pluginManager: PluginManager

        @get:Inject
        protected abstract val project: Project

        protected val logger: Logger = Logging.getLogger(this::class.simpleName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: D,
            buildModel: ComposeBuildModel,
            parentDefinition: P
        ) {
            logger.info("Applying Compose Multiplatform software feature to the project.")

            pluginManager.apply("org.jetbrains.compose")

            context(context, buildModel) {
                initialize(definition, parentDefinition)
            }
        }

        context(context: ProjectFeatureApplicationContext, buildModel: ComposeBuildModel)
        open fun initialize(definition: D, parentDefinition: P): Unit = Unit

        @Suppress("unused")
        protected inline fun withComposeMultiplatform(crossinline action: ComposeExtension.() -> Unit) {
            pluginManager.withPlugin("org.jetbrains.compose") {
                project.extensions.getByType(ComposeExtension::class.java).action()
            }
        }
    }

    internal abstract class ComposeLibrarySoftwareFeatureApplyAction : ComposeSoftwareFeatureApplyAction<ComposeDefinition, LibraryProjectType>() {

        context(context: ProjectFeatureApplicationContext, buildModel: ComposeBuildModel)
        override fun initialize(definition: ComposeDefinition, parentDefinition: LibraryProjectType) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        }
    }

    internal abstract class ComposeAndroidApplicationSoftwareFeatureApplyAction : ComposeSoftwareFeatureApplyAction<ComposeAndroidDefinition, ApplicationDeclarativeDefinition>() {

        context(context: ProjectFeatureApplicationContext, buildModel: ComposeBuildModel)
        override fun initialize(definition: ComposeAndroidDefinition, parentDefinition: ApplicationDeclarativeDefinition) {
            // NOTE: Android project types already apply the Compose Compiler plugin by default,
            // no need to apply it here (again) as with LibraryProjectType
            parentDefinition.buildFeatures.compose = true
            parentDefinition.composeOptions.kotlinCompilerExtensionVersion = definition.compilerExtensionVersion.orNull
            // Compose runtime is not present for Android projects by default
            project.dependencies.add("implementation", "androidx.compose.runtime:runtime:+")
        }
    }
}