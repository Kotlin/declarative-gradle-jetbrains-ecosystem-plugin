package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import java.util.Locale.getDefault
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
                services.pluginManager.apply("org.jetbrains.kotlin.jvm")

                val kotlinJvmExtension = services.project.extensions.getByType(KotlinJvmExtension::class.java)
                kotlinJvmExtension.registerApplication(
                    KotlinCompilation.MAIN_COMPILATION_NAME,
                    definition,
                    buildModel,
                    services,
                    context.objectFactory,
                )
            }
                .withUnsafeApplyAction()
                .withBuildModelImplementationType(DefaultJvmApplicationBuildModel::class.java)
        }

        private fun KotlinJvmExtension.registerApplication(
            name: String,
            definition: ApplicationDefinition,
            buildModel: JvmApplicationBuildModel,
            services: Services,
            objectFactory: ObjectFactory,
        ) {
            buildModel.applications.create("main") { application ->
                application.mainClassName.convention(definition.mainClass)
                application.applicationName.convention(definition.name.orElse(services.project.name))
                application.moduleName.convention(definition.moduleName)
                application.jvmArgs.convention(definition.jvmArgs)

                val mainCompilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
                application.compiledClasses.from(mainCompilation.output.allOutputs)
                application.dependencies.from(
                    mainCompilation.runtimeDependencyFiles
                )

                application.executionDirectory.convention(services.projectLayout.buildDirectory.dir("application/$name"))
                (application as InternalJvmApplication).runTaskProvider = services.tasksContainer
                    .registerJvmApplicationRunTask(application, objectFactory)
            }
        }

        private fun TaskContainer.registerJvmApplicationRunTask(
            application: JvmApplication,
            objectFactory: ObjectFactory,
        ): TaskProvider<JavaExec> =
            register(application.runTaskName, JavaExec::class.java) { javaExecTask ->
                javaExecTask.description = "Runs this project as a JVM application"
                javaExecTask.group = ApplicationPlugin.APPLICATION_GROUP

                val runtimeClasspath = objectFactory.fileCollection().from(
                    application.compiledClasses,
                    application.dependencies,
                )
                javaExecTask.classpath(runtimeClasspath)
                javaExecTask.mainClass.value(application.mainClassName)
                javaExecTask.mainModule.value(application.moduleName)
                javaExecTask.jvmArguments.value(application.jvmArgs)
                //TODO: javaExecTask.modularity.inferModulePath
                //TODO: javaExecTask.javaLauncher
                javaExecTask.workingDir(
                    application.executionDirectory.map {
                        it.asFile.mkdirs()
                        it
                    }
                )
            }

        private val JvmApplication.runTaskName
            get() = if (name == KotlinCompilation.MAIN_COMPILATION_NAME) "run" else "run${
                name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        getDefault()
                    ) else it.toString()
                }
            }"

        internal interface Services {
            // Unsafe service for apply action
            @get:Inject
            val pluginManager: PluginManager

            @get:Inject
            val project: Project

            @get:Inject
            val projectLayout: ProjectLayout

            @get:Inject
            val tasksContainer: TaskContainer
        }
    }
}
