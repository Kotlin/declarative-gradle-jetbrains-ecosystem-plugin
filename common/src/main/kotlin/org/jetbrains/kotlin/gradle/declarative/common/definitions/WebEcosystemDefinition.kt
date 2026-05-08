package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested

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