package org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.jetbrains.kotlin.gradle.declarative.common.definitions.TestingExtension

public interface JvmApplicationTestingExtension : TestingExtension {
    @get:Nested
    public val suites: NamedDomainObjectContainer<KotlinTestSuite>
}

public interface KotlinTestSuite : Definition<KotlinTestSuiteBuildModel>, Named {
    public val useJUnitPlatform: Property<Boolean>

    @get:Nested
    public val targets: NamedDomainObjectContainer<KotlinTestSuiteTarget>

    @get:Nested
    public val dependencies: JvmApplicationTestingDependenciesExtension
}

public interface KotlinTestSuiteBuildModel : BuildModel {
    public val testSuite: JvmTestSuite
}

internal abstract class DefaultKotlinTestSuiteBuildModel : KotlinTestSuiteBuildModel {
    override lateinit var testSuite: JvmTestSuite
}

public interface KotlinTestSuiteTarget :
    Named,
    Definition<KotlinTestSuiteTargetBuildModel> {
    public val dependsOnCheck: Property<Boolean>

    // JavaForkOptions uses Any/Object, that is not supported in DCL
    @get:Nested
    public val javaForkOptions: JavaDclForkOptions
}

public interface KotlinTestSuiteTargetBuildModel : BuildModel {
    public val target: JvmTestSuiteTarget
}
internal abstract class DefaultKotlinTestSuiteTargetBuildModel : KotlinTestSuiteTargetBuildModel {
    override lateinit var target: JvmTestSuiteTarget
}

public interface JavaDclForkOptions {
    public val environment: MapProperty<String, String>
}
