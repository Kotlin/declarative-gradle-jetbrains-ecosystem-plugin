package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.model.ObjectFactory

public sealed interface IosApplicationEntryPointModifiers {

    public val auto: IosApplicationEntryPoint.Auto

    public fun reference(qualifiedRefence: String): IosApplicationEntryPoint.Reference
}

public fun ObjectFactory.newIosApplicationEntryPointModifiers(): IosApplicationEntryPointModifiers =
    newInstance(IosApplicationEntryPointModifiersImpl::class.java)