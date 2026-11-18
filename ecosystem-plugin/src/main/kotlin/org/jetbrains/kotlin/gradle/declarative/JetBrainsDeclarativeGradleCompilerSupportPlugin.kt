package org.jetbrains.kotlin.gradle.declarative

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal class JetBrainsDeclarativeGradleCompilerSupportPlugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        kotlinCompilation.defaultSourceSet.dependencies {
            implementation("org.jetbrains.ecosystem:annotations:+")
        }
        kotlinCompilation.compileTaskProvider.configure {
            it.compilerOptions.freeCompilerArgs.add("-Xcompiler-plugin-order=${getCompilerPluginId()}>androidx.compose.compiler.plugins.kotlin")
        }
        return kotlinCompilation.target.project.provider { emptyList() }
    }

    override fun getCompilerPluginId(): String = "org.jetbrains.kotlin.gradle.declarative.compiler"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact("org.jetbrains.ecosystem", "compiler-plugin", "0.0.1-SNAPSHOT")

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}