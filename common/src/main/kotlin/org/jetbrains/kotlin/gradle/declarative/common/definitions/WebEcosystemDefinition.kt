package org.jetbrains.kotlin.gradle.declarative.common.definitions

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested

public interface WebEcosystemDefinition : EcosystemWithSubplatforms<WebSubplatform> {
    @get:Nested
    public val kotlin: KotlinWebCompilationExtension

    /**
     * See [WebSubplatform] for available values.
     */
    public override val subplatforms: ListProperty<WebSubplatform>
}

public enum class WebSubplatform : Subplatform {
    js, wasmJs;
}