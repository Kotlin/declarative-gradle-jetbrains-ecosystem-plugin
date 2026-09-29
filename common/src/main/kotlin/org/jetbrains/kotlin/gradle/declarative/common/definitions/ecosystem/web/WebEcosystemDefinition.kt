package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.web

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.EcosystemWithSubplatforms
import org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem.Subplatform
import org.jetbrains.kotlin.gradle.declarative.common.definitions.compilation.KotlinWebCompilationExtension

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