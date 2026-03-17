package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

public class JetbrainsEcosystemPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        println("JetbrainsEcosystemPlugin applied to settings")
    }
}
