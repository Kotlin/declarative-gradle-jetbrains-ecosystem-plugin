package org.jetbrains.kotlin.gradle.declarative

internal sealed class GeneratedXcodeEntryPointImpl : GeneratedXcodeEntryPoint {

    internal data object Auto : GeneratedXcodeEntryPointImpl(), GeneratedXcodeEntryPoint.Auto

    internal data class Reference(override val qualifiedReference: String) : GeneratedXcodeEntryPointImpl(), GeneratedXcodeEntryPoint.Reference
}

internal abstract class GeneratedXcodeEntryPointModifiersImpl : GeneratedXcodeEntryPointModifiers {

    override val auto: GeneratedXcodeEntryPoint.Auto get() = GeneratedXcodeEntryPointImpl.Auto

    override fun reference(qualifiedRefence: String) = GeneratedXcodeEntryPointImpl.Reference(qualifiedRefence)
}