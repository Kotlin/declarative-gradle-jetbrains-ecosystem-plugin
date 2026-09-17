package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.testfixtures

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(JvmTestFixturesFeature.Binding::class)
public abstract class JvmTestFixturesFeature : Plugin<Project> {
    override fun apply(target: Project) {}

    public class Binding : ProjectFeatureBinding {

        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder.bindProjectFeature("testFixtures", ApplyAction::class)
                .withUnsafeApplyAction()
                // https://github.com/gradle/gradle/issues/36755
                .withUnsafeDefinition()
        }

        internal abstract class ApplyAction :
            ProjectFeatureApplyAction<TestFixturesDefinition, BuildModel.None, JvmApplicationProjectType> {

            @get:Inject
            abstract val pluginManager: PluginManager
            @get:Inject
            abstract val configurations: ConfigurationContainer

            override fun apply(
                context: ProjectFeatureApplicationContext,
                definition: TestFixturesDefinition,
                buildModel: BuildModel.None,
                parentDefinition: JvmApplicationProjectType,
            ) {
                pluginManager.apply("java-test-fixtures")
                configurations.wire("testFixtures", definition.dependencies)
            }
        }
    }
}
