package org.jetbrains.kotlin.gradle.declarative.projecttypes

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType

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

    internal abstract class LibraryApplyAction : ProjectTypeApplyAction<LibraryProjectType, LibraryBuildModel> {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: LibraryProjectType,
            buildModel: LibraryBuildModel
        ) {

        }
    }
}