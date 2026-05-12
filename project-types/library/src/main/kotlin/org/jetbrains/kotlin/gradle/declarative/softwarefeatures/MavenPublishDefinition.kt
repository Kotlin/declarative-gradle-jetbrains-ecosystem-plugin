package org.jetbrains.kotlin.gradle.declarative.softwarefeatures

import org.gradle.api.provider.Property
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import java.net.URI

@Suppress("UnstableApiUsage")
public interface MavenPublishDefinition : Definition<BuildModel.None> {
    public val name: Property<String>
    public val repositoryUrl: Property<String>
    public val withDocs: Property<Boolean>
    public val withSources: Property<Boolean>
}