package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.features.binding.BuildModel
import javax.inject.Inject

@Suppress("UnstableApiUsage")
public interface JvmApplicationBuildModel : BuildModel {
    public val applications: NamedDomainObjectContainer<JvmApplication>
}

public interface JvmApplication : Named {
    public val mainClassName: Property<String>
    public val applicationName: Property<String>
    public val moduleName: Property<String>
    public val jvmArgs: ListProperty<String>

    public val compiledClasses: ConfigurableFileCollection
    public val dependencies: ConfigurableFileCollection

    public val runTask: TaskProvider<JavaExec>
    public val executionDirectory: DirectoryProperty
}

internal interface InternalJvmApplication : JvmApplication {
    var runTaskProvider: TaskProvider<JavaExec>
    override val runTask: TaskProvider<JavaExec>
        get() = runTaskProvider
}

internal abstract class DefaultJvmApplicationBuildModel @Inject constructor(
    objectFactory: ObjectFactory,
) : JvmApplicationBuildModel {
    @Suppress("UNCHECKED_CAST")
    override val applications: NamedDomainObjectContainer<JvmApplication> = objectFactory
        .domainObjectContainer(InternalJvmApplication::class.java) as NamedDomainObjectContainer<JvmApplication>
}

