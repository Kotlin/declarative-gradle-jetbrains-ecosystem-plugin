package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal abstract class DefaultIosApplicationProjectType
@Inject constructor(objectFactory: ObjectFactory) : IosApplicationProjectType {

    override val xcodeProject: XcodeProjectGenerationExtension =
        objectFactory.newInstance(DefaultXcodeProjectGenerationExtension::class.java)
}