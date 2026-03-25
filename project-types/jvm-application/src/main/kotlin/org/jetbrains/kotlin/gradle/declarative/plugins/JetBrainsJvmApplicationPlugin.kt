package org.jetbrains.kotlin.gradle.declarative.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.jetbrains.kotlin.gradle.declarative.projecttypes.JvmApplicationProjectType
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@BindsProjectType(JetBrainsJvmApplicationPlugin.Binding::class)
public abstract class JetBrainsJvmApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Applying JetBrains JVM Application plugin to ${target.path}")
    }

    public class Binding : ProjectTypeBinding {
        override fun bind(builder: ProjectTypeBindingBuilder) {
            builder.bindProjectType(
                "jvmApplication",
                JvmApplicationProjectType::class.java
            ) { context, definition, buildModel ->
                val services = context.objectFactory.newInstance(Services::class.java)

                // Recommended for now way to apply Gradle and 3rd party plugins, `apply(project)` method usage will be
                // forbidden in the future releases
                services.pluginManager.apply("application")
                services.pluginManager.apply("org.jetbrains.kotlin.jvm")
            }
                .withUnsafeApplyAction()
        }

        internal interface Services {
            // Unsafe service for apply action
            @get:Inject
            val pluginManager: PluginManager
        }
    }
}