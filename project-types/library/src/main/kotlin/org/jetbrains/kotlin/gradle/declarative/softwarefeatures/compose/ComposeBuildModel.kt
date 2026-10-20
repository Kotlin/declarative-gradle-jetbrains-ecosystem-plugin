package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.compose

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.features.binding.BuildModel
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import javax.inject.Inject
import kotlin.jvm.java

@Suppress("UnstableApiUsage")
public interface ComposeBuildModel : BuildModel {

    public val resourcesConfiguration: ComposeResourcesConfiguration

    public val compilerConfiguration: ComposeCompilerConfiguration
}

public interface ComposeResourcesConfiguration {

    public val isResourceClassPublic: Property<Boolean>

    public val resourceClassPackage: Property<String>

    public val resourceClassName: Property<String>

    public val resourceClassGeneration: Property<ResourcesExtension.ResourceClassGeneration>

    public fun customDirectoryForSourceSet(sourceSetName: String, directory: Provider<Directory>)
}

public interface ComposeCompilerConfiguration {

    public val includeSourceInformation: Property<Boolean>

    public val metricsDestination: DirectoryProperty

    public val reportsDestination: DirectoryProperty

    public val stabilityConfigurationFiles: ListProperty<RegularFile>

    public val includeTraceMarkers: Property<Boolean>

    public val targetKotlinPlatforms: SetProperty<KotlinPlatformType>

    public val includeComposeMappingFile: Property<Boolean>
}

internal abstract class DefaultComposeBuildModel @Inject constructor(project: Project) : ComposeBuildModel {

    override val resourcesConfiguration: ComposeResourcesConfiguration =
        project.objects.newInstance(DefaultComposeResourcesConfiguration::class.java, project)

    override val compilerConfiguration: ComposeCompilerConfiguration =
        project.objects.newInstance(DefaultComposeCompilerConfiguration::class.java, project)
}

internal abstract class DefaultComposeResourcesConfiguration @Inject constructor(project: Project) : ComposeResourcesConfiguration {

    private val composeResources: Provider<ResourcesExtension> = project.provider {
        project.extensions.getByType(ResourcesExtension::class.java)
    }

    override val isResourceClassPublic: Property<Boolean> =
        project.propertyFromVar(
            getter = composeResources.map(ResourcesExtension::publicResClass),
            setter = { composeResources.get().publicResClass = it }
        )

    override val resourceClassPackage: Property<String> =
        project.propertyFromVar(
            getter = context(project) { resourcePackage },
            setter = { composeResources.get().packageOfResClass = it }
        )

    override val resourceClassName: Property<String> =
        project.propertyFromVar(
            getter = composeResources.map(ResourcesExtension::nameOfResClass),
            setter = { composeResources.get().nameOfResClass = it }
        )

    override val resourceClassGeneration: Property<ResourcesExtension.ResourceClassGeneration> =
        project.propertyFromVar(
            getter = composeResources.map(ResourcesExtension::generateResClass),
            setter = { composeResources.get().generateResClass = it }
        )

    override fun customDirectoryForSourceSet(sourceSetName: String, directory: Provider<Directory>) =
        composeResources.get().customDirectory(sourceSetName, directory)

    context(project: Project)
    private val resourcePackage: Provider<String> get() = composeResources.map {
        it.packageOfResClass.takeIf(String::isNotEmpty) ?: run {
            val groupName = project.group.toString().lowercase().asUnderscoredIdentifier()
            val moduleName = project.name.lowercase().asUnderscoredIdentifier()
            val id = if (groupName.isNotEmpty()) "$groupName.$moduleName" else moduleName
            "$id.generated.resources"
        }
    }

    private fun String.asUnderscoredIdentifier(): String =
        replace('-', '_')
            .let { if (it.isNotEmpty() && it.first().isDigit()) "_$it" else it }
}

internal abstract class DefaultComposeCompilerConfiguration @Inject constructor(project: Project) : ComposeCompilerConfiguration {

    private val composeCompiler: Provider<ComposeCompilerGradlePluginExtension> = project.provider {
        project.extensions.getByType(ComposeCompilerGradlePluginExtension::class.java)
    }

    override val includeSourceInformation: Property<Boolean>
        get() = composeCompiler.get().includeSourceInformation

    override val metricsDestination: DirectoryProperty
        get() = composeCompiler.get().metricsDestination

    override val reportsDestination: DirectoryProperty
        get() = composeCompiler.get().reportsDestination

    override val stabilityConfigurationFiles: ListProperty<RegularFile>
        get() = composeCompiler.get().stabilityConfigurationFiles

    override val includeTraceMarkers: Property<Boolean>
        get() = composeCompiler.get().includeTraceMarkers

    override val targetKotlinPlatforms: SetProperty<KotlinPlatformType>
        get() = composeCompiler.get().targetKotlinPlatforms

    override val includeComposeMappingFile: Property<Boolean>
        get() = composeCompiler.get().includeComposeMappingFile
}