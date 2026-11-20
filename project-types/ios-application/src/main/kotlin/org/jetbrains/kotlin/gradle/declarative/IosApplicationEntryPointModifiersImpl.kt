package org.jetbrains.kotlin.gradle.declarative

internal sealed class IosApplicationEntryPointImpl : IosApplicationEntryPoint {

    internal data object Auto : IosApplicationEntryPointImpl(), IosApplicationEntryPoint.Auto

    internal data class Reference(override val qualifiedReference: String) : IosApplicationEntryPointImpl(), IosApplicationEntryPoint.Reference
}

internal abstract class IosApplicationEntryPointModifiersImpl : IosApplicationEntryPointModifiers {

    override val auto: IosApplicationEntryPoint.Auto get() = IosApplicationEntryPointImpl.Auto

    override fun reference(qualifiedRefence: String) = IosApplicationEntryPointImpl.Reference(qualifiedRefence)
}