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
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.JavaJvmCompilationType
import org.jetbrains.kotlin.gradle.declarative.common.buildtypes.KotlinJvmCompilationType
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
            builder
                .bindProjectType("jvmApplication", JvmApplicationApplyAction::class)
                .withUnsafeApplyAction()
                .withBuildModelImplementationType(DefaultJvmApplicationBuildModel::class.java)
        }
    }

    internal abstract class JvmApplicationApplyAction :
        ProjectTypeApplyAction<JvmApplicationProjectType, JvmApplicationBuildModel> {

        // Unsafe service for apply action
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val projectLayout: ProjectLayout

        @get:Inject
        abstract val tasksContainer: TaskContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: JvmApplicationProjectType,
            buildModel: JvmApplicationBuildModel
        ) {
            // Recommended for now way to apply Gradle and 3rd party plugins, `apply(project)` method usage will be
            // forbidden in the future releases
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            val kotlinJvmExtension = project.extensions.getByType(KotlinJvmExtension::class.java)
            kotlinJvmExtension.bindMainCompilation(
                buildModel as DefaultJvmApplicationBuildModel
            )
            kotlinJvmExtension.registerApplication(
                KotlinCompilation.MAIN_COMPILATION_NAME,
                definition,
                buildModel,
                context.objectFactory,
            )
        }

        private fun KotlinJvmExtension.bindMainCompilation(
            buildModel: DefaultJvmApplicationBuildModel,
        ) {
            val mainCompilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

            buildModel.compilationUnits
                .create(KotlinCompilation.MAIN_COMPILATION_NAME) { kotlinJvmCompilationUnit ->
                    kotlinJvmCompilationUnit as DefaultJvmApplicationBuildModel.DefaultJvmCompilationUnit
                    kotlinJvmCompilationUnit.kotlinCompilation = mainCompilation
                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "kotlin",
                        KotlinJvmCompilationType::class.java
                    ) { kotlinCompilation ->
                        // TODO: wire properly
                    }
                    kotlinJvmCompilationUnit.jvmCompilations.create(
                        "java",
                        JavaJvmCompilationType::class.java
                    ) { javaCompilation ->
                        // TODO: wire properly
                    }
            }
        }

        private fun KotlinJvmExtension.registerApplication(
            name: String,
            definition: ApplicationDefinition,
            buildModel: JvmApplicationBuildModel,
            objectFactory: ObjectFactory,
        ) {
            buildModel.applications.create("main") { application ->
                application.mainClassName.convention(definition.mainClass)
                application.applicationName.convention(definition.name.orElse(project.name))
                application.moduleName.convention(definition.moduleName)
                application.jvmArgs.convention(definition.jvmArgs)

                val mainCompilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
                application.compiledClasses.from(mainCompilation.output.allOutputs)
                application.runtimeDependencies.from(
                    mainCompilation.runtimeDependencyFiles
                )

                application.executionDirectory.convention(projectLayout.buildDirectory.dir("application/$name"))
                (application as InternalJvmApplication).runTaskProvider = tasksContainer
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
                    application.runtimeDependencies,
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
    }
}
