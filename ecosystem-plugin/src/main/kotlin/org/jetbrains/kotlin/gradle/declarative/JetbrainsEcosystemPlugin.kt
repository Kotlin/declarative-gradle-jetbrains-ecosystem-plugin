package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.features.annotations.RegistersProjectFeatures
import org.jetbrains.kotlin.gradle.declarative.projecttype.JetBrainsJvmApplicationPlugin
import org.jetbrains.kotlin.gradle.declarative.projectfeature.distribution.DistributionProjectFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.projectfeature.kotlinserialization.KotlinSerializationProjectFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.projecttype.JetBrainsLibraryPlugin
import org.jetbrains.kotlin.gradle.declarative.projecttype.WebApplicationPlugin
import org.jetbrains.kotlin.gradle.declarative.projectfeature.resource.ResourceProjectFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.projectfeature.mavenpublish.MavenPublishProjectFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.projectfeature.iosframework.IosFrameworkPublishingProjectFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.projectfeature.spring.SpringProjectFeaturePlugin

@Suppress("UnstableApiUsage")
@RegistersProjectFeatures(
    JetBrainsJvmApplicationPlugin::class,
    WebApplicationPlugin::class,
    JetBrainsLibraryPlugin::class,
    DistributionProjectFeaturePlugin::class,
    KotlinSerializationProjectFeaturePlugin::class,
    SpringProjectFeaturePlugin::class,
    ResourceProjectFeaturePlugin::class,
    MavenPublishProjectFeaturePlugin::class,
    IosFrameworkPublishingProjectFeaturePlugin::class,
)
public class JetbrainsEcosystemPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(JetbrainsEcosystemPlugin::class.java)

    override fun apply(target: Settings) {
        logger.info("JetbrainsEcosystemPlugin applied to settings")
    }
}
