package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.features.annotations.RegistersProjectFeatures
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JetBrainsJvmApplicationPlugin
import org.jetbrains.kotlin.gradle.declarative.softwarefeatures.distribution.DistributionSoftwareFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.common.softwarefeatures.kotlinserialization.KotlinSerializationSoftwareFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.projecttypes.JetBrainsLibraryPlugin
import org.jetbrains.kotlin.gradle.declarative.projecttypes.webapplication.WebApplicationPlugin
import org.jetbrains.kotlin.gradle.declarative.softwarefeature.resource.ResourceSoftwareFeaturePlugin
import org.jetbrains.kotlin.gradle.declarative.softwarefeatures.spring.SpringSoftwareFeaturePlugin

@Suppress("UnstableApiUsage")
@RegistersProjectFeatures(
    JetBrainsJvmApplicationPlugin::class,
    WebApplicationPlugin::class,
    JetBrainsLibraryPlugin::class,
    DistributionSoftwareFeaturePlugin::class,
    KotlinSerializationSoftwareFeaturePlugin::class,
    SpringSoftwareFeaturePlugin::class,
    ResourceSoftwareFeaturePlugin::class,
)
public class JetbrainsEcosystemPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(JetbrainsEcosystemPlugin::class.java)
    private val MIN_SUPPORTED_GRADLE_VERSION = "9.6.0-milestone-1"

    override fun apply(target: Settings) {
        logger.info("JetbrainsEcosystemPlugin applied to settings")
        checkMinimalSupportedGradleVersion()
    }

    private fun checkMinimalSupportedGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version(MIN_SUPPORTED_GRADLE_VERSION)) {
            logger.error(
                """
                Minimal supported Gradle version is $MIN_SUPPORTED_GRADLE_VERSION and project is using ${GradleVersion.current()}.
                Update it by running: ./gradlew wrapper wrapper --distribution-type=BIN --gradle-version=$MIN_SUPPORTED_GRADLE_VERSION
                """.trimIndent()
            )
            throw GradleException("Unsupported Gradle release")
        }
    }
}
