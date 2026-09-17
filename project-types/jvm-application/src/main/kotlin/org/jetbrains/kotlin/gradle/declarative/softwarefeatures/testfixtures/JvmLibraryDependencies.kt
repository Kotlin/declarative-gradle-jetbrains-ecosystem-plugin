package org.jetbrains.kotlin.gradle.declarative.softwarefeatures.testfixtures

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.JvmComponentDependencies

// Should be part of core Gradle once
// https://github.com/gradle/gradle/issues/32726 is implemented
public interface JvmLibraryDependencies : JvmComponentDependencies {
    /**
     * Returns a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of api dependencies.
     *
     *
     * `api` dependencies are used at compilation and runtime.
     *
     * @return a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of api dependencies
     */
    public val api: DependencyCollector

    /**
     * Returns a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of compile-only api dependencies.
     *
     *
     * `compileOnlyApi` dependencies are used only at compilation and are not available at runtime.
     *
     * @return a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of compile-only api dependencies
     */
    public val compileOnlyApi: DependencyCollector
}

internal fun ConfigurationContainer.wire(prefix: String, dependencies: JvmLibraryDependencies) {
    named("${prefix}Api".replaceFirstChar { it.lowercase() }) {
        it.fromDependencyCollector(dependencies.api)
    }
    named("${prefix}CompileOnlyApi".replaceFirstChar { it.lowercase() }) {
        it.fromDependencyCollector(dependencies.compileOnlyApi)
    }
    wire(prefix, dependencies as JvmComponentDependencies)
}

internal fun ConfigurationContainer.wire(prefix: String, dependencies: JvmComponentDependencies) {
    named("${prefix}Implementation".replaceFirstChar { it.lowercase() }) {
        it.fromDependencyCollector(dependencies.implementation)
    }
    named("${prefix}CompileOnly".replaceFirstChar { it.lowercase() }) {
        it.fromDependencyCollector(dependencies.compileOnly)
    }
    named("${prefix}RuntimeOnly".replaceFirstChar { it.lowercase() }) {
        it.fromDependencyCollector(dependencies.runtimeOnly)
    }
    named("${prefix}AnnotationProcessor".replaceFirstChar { it.lowercase() }) {
        it.fromDependencyCollector(dependencies.annotationProcessor)
    }
}
