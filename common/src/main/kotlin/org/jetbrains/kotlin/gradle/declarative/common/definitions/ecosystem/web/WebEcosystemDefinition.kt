package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.web

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinWebCompilationExtension

public interface WebEcosystemDefinition {
    @get:Nested
    public val kotlin: KotlinWebCompilationExtension

    // Fixme convert to enum
    /**
     * Accepts [WebSubplatforms] entries as strings.
     */
    public val subplatforms: ListProperty<String>
}

public enum class WebSubplatforms {
    js, wasmJs;
}