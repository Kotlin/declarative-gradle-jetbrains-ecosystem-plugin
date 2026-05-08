@file:Suppress("INVISIBLE_REFERENCE")

package org.jetbrains.kotlin.gradle.declarative.projecttypes.webapplication

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginManager
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.declarative.common.definitions.WebSubplatforms
import org.jetbrains.kotlin.gradle.declarative.common.sync.syncKotlinJsCompilerOptionsAsConvention
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

        @get:Inject
        abstract val objectFactory: ObjectFactory

        private val logger = Logging.getLogger(WebApplicationPlugin::class.qualifiedName)

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: WebApplicationProjectType,
            buildModel: WebApplicationBuildModel
        ) {
            logger.info("Applying JetBrains Web Application plugin to ${project.path}")

            pluginManager.apply("org.jetbrains.kotlin.multiplatform")

            val kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            val enabledSubplatforms = definition.subplatforms
                .map {
                    it.map { name ->
                        WebSubplatforms.entries.find { it.name.equals(name, ignoreCase = true) }
                            ?: throw GradleException(
                                "Unknown subplatform $name, accepted subplatforms: ${WebSubplatforms.entries.joinToString { it.name }}"
                            )
                    }
                }
                .getOrElse(WebSubplatforms.entries.toList())
                // ListProperty default value is empty list
                .ifEmpty { WebSubplatforms.entries.toList() }

            val jsTarget = if (enabledSubplatforms.contains(WebSubplatforms.js)) {
                kmpExtension.js {
                    browser()
                    binaries.executable()
                }
            } else null

            @OptIn(ExperimentalWasmDsl::class)
            val wasmTarget = if (enabledSubplatforms.contains(WebSubplatforms.wasmJs)) {
                kmpExtension.wasmJs {
                    browser()
                    binaries.executable()
                }
            } else null

            kmpExtension.applyDefaultHierarchyTemplate()

            val webMainSourceSet = kmpExtension.sourceSets.getByName("webMain")

            definition.dependencies.implementation.dependencies.getOrElse(emptySet()).forEach { dependency ->
                webMainSourceSet.dependencies { implementation(dependency) }
            }

            jsTarget?.let {
                val defaultJsCompilerOptions = objectFactory.newInstance(jsTarget.compilerOptions.javaClass)
                syncKotlinJsCompilerOptionsAsConvention(
                    from = definition.kotlin.compilerOptions,
                    into = jsTarget.compilerOptions,
                    fallback = defaultJsCompilerOptions,
                )
            }

            wasmTarget?.let {
                val defaultWasmCompilerOptions = objectFactory.newInstance(wasmTarget.compilerOptions.javaClass)

                syncKotlinJsCompilerOptionsAsConvention(
                    from = definition.kotlin.compilerOptions,
                    into = wasmTarget.compilerOptions,
                    fallback = defaultWasmCompilerOptions,
                )
            }
        }
    }
}