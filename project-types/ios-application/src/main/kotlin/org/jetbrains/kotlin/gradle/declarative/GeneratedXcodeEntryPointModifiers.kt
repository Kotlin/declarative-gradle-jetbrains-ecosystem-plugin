package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.model.ObjectFactory

public sealed interface GeneratedXcodeEntryPointModifiers {

    public val auto: GeneratedXcodeEntryPoint.Auto

    public fun reference(qualifiedRefence: String): GeneratedXcodeEntryPoint.Reference
}

public fun ObjectFactory.newGeneratedXcodeEntryPointModifiers(): GeneratedXcodeEntryPointModifiers =
    newInstance(GeneratedXcodeEntryPointModifiersImpl::class.java)