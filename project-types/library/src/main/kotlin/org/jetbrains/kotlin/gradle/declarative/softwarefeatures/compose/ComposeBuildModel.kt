package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.compose

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.features.binding.BuildModel
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface ComposeBuildModel : BuildModel {

    public val isResourceClassPublic: Property<Boolean>

    public val resourceClassPackage: Property<String>

    public val resourceClassName: Property<String>

    public val resourceClassGeneration: Property<ResourcesExtension.ResourceClassGeneration>

    public fun customDirectoryForSourceSet(sourceSetName: String, directory: Provider<Directory>)
}

internal abstract class DefaultComposeBuildModel @Inject constructor(project: Project) : ComposeBuildModel {
    internal lateinit var composeResources: ResourcesExtension

    override val isResourceClassPublic: Property<Boolean> =
        project.propertyFromVar(
            getter = { composeResources.publicResClass },
            setter = { composeResources.publicResClass = it }
        )

    override val resourceClassPackage: Property<String> =
        project.propertyFromVar(
            getter = { composeResources.resourcePackage },
            setter = { composeResources.packageOfResClass = it }
        )

    override val resourceClassName: Property<String> =
        project.propertyFromVar(
            getter = { composeResources.nameOfResClass },
            setter = { composeResources.nameOfResClass = it }
        )

    override val resourceClassGeneration: Property<ResourcesExtension.ResourceClassGeneration> =
        project.propertyFromVar(
            getter = { composeResources.generateResClass },
            setter = { composeResources.generateResClass = it }
        )

    override fun customDirectoryForSourceSet(sourceSetName: String, directory: Provider<Directory>) =
        composeResources.customDirectory(sourceSetName, directory)
}

internal fun ComposeExtension.initializeBuildModel(buildModel: ComposeBuildModel) {
    (buildModel as DefaultComposeBuildModel).composeResources = extensions.getByType(ResourcesExtension::class.java)
}