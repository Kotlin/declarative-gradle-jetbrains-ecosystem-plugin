package org.jetbrains.kotlin.gradle.declarative.compiler.diagnostics

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement

internal object FirIosApplicationAnnotationDiagnostics  : KtDiagnosticsContainer() {

    val MUST_BE_TOP_LEVEL_FUNCTION: KtDiagnosticFactory0 by error0<KtElement>()

    val MUST_BE_PUBLIC: KtDiagnosticFactory0 by error0<KtElement>()

    val MUST_RETURN_UI_VIEW_CONTROLLER_TYPE: KtDiagnosticFactory0 by error0<KtElement>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = object : BaseDiagnosticRendererFactory() {
        override val MAP by KtDiagnosticFactoryToRendererMap("JetBrains Declarative Gradle Ios Application Annotation Diagnostics") {
            it.put(
                MUST_BE_TOP_LEVEL_FUNCTION,
                "@IosApplicationEntryPoint is only applicable to top-level functions."
            )
            it.put(
                MUST_BE_PUBLIC,
                "Functions annotated with @IosApplicationEntryPoint must be public."
            )
            it.put(
                MUST_RETURN_UI_VIEW_CONTROLLER_TYPE,
                "Functions annotated with @IosApplicationEntryPoint must explicitly return an UIViewController instance."
            )
        }
    }
}