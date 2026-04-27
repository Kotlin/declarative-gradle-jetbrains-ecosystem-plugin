package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.features.annotations.RegistersProjectFeatures
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JetBrainsJvmApplicationPlugin
import org.jetbrains.kotlin.gradle.declarative.softwarefeatures.distribution.DistributionSoftwareFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.common.softwarefeatures.kotlinserialization.KotlinSerializationSoftwareFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.softwarefeatures.spring.SpringSoftwareFeaturePlugin

@Suppress("UnstableApiUsage")
@RegistersProjectFeatures(
    JetBrainsJvmApplicationPlugin::class,
    DistributionSoftwareFeaturePlugin::class,
    KotlinSerializationSoftwareFeaturePlugin::class,
    SpringSoftwareFeaturePlugin::class,
)
public class JetbrainsEcosystemPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(JetbrainsEcosystemPlugin::class.java)

    override fun apply(target: Settings) {
        logger.info("JetbrainsEcosystemPlugin applied to settings")
    }
}
