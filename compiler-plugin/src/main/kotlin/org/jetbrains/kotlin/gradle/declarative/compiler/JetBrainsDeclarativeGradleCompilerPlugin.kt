package org.jetbrains.kotlin.gradle.declarative.compiler

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.AnnotationBasedExtension
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirNamedFunctionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.gradle.declarative.compiler.diagnostics.FirIosApplicationAnnotationDiagnostics
import org.jetbrains.kotlin.gradle.declarative.compiler.checkers.FirIosApplicationAnnotationChecker

@OptIn(ExperimentalCompilerApi::class)
public class JetBrainsDeclarativeGradleCompilerPlugin : CompilerPluginRegistrar() {

    override val pluginId: String = "org.jetbrains.kotlin.gradle.declarative.compiler"

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(object : FirExtensionRegistrar() {

            override fun ExtensionRegistrarContext.configurePlugin() {
                +{ session: FirSession ->
                    object : FirAdditionalCheckersExtension(session) {
                        override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
                            override val namedFunctionCheckers: Set<FirNamedFunctionChecker> get() = setOf(
                                FirIosApplicationAnnotationChecker,
                            )
                        }
                    }
                }
                registerDiagnosticContainers(FirIosApplicationAnnotationDiagnostics)
            }
        })
    }
}