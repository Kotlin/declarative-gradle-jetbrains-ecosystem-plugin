package org.jetbrains.kotlin.gradle.declarative.common.definitions.ecosystem

import org.gradle.api.provider.ListProperty

public interface Subplatform

public interface EcosystemWithSubplatforms<S : Subplatform> {

    public val subplatforms: ListProperty<S>
}