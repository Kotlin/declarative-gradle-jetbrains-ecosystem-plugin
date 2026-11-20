package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal abstract class DefaultXcodeProjectGenerationExtension
@Inject constructor(objectFactory: ObjectFactory) :
    XcodeProjectGenerationExtension,
    IosApplicationEntryPointModifiers by objectFactory.newIosApplicationEntryPointModifiers()