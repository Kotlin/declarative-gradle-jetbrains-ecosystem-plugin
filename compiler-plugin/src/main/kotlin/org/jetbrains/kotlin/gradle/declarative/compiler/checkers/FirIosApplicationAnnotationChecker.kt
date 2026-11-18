package org.jetbrains.kotlin.gradle.declarative.compiler.checkers

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirNamedFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.isTopLevel
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotationSafe
import org.jetbrains.kotlin.fir.declarations.utils.compilerPluginMetadata
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.gradle.declarative.compiler.diagnostics.FirIosApplicationAnnotationDiagnostics
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object FirIosApplicationAnnotationChecker : FirNamedFunctionChecker(MppCheckerKind.Platform) {

    private val UI_KIT_PACKAGE_NAME = FqName("platform.UIKit")

    private val DG_ANNOTATIONS_PACKAGE = FqName("org.jetbrains.kotlin.gradle.declarative.compiler.annotations")

    private val UI_VIEW_CONTROLLER = ClassId(UI_KIT_PACKAGE_NAME, Name.identifier("UIViewController"))

    private val IOS_APP_ENTRY_POINT = ClassId(DG_ANNOTATIONS_PACKAGE, Name.identifier("IosApplicationEntryPoint"))

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirNamedFunction) {
        // Expand typealiases if necessary
        val hasAnnotation = declaration.hasAnnotationSafe(IOS_APP_ENTRY_POINT, context.session)
        if (!hasAnnotation) return

        val isTopLevel = context.isTopLevel
        if (!isTopLevel) return reporter.reportOn(declaration.source, FirIosApplicationAnnotationDiagnostics.MUST_BE_TOP_LEVEL_FUNCTION)

        val isPublic = declaration.visibility == Visibilities.Public
        if (!isPublic) return reporter.reportOn(declaration.source, FirIosApplicationAnnotationDiagnostics.MUST_BE_PUBLIC)

        val returnsUiViewController = declaration.symbol.resolvedReturnType.classId == UI_VIEW_CONTROLLER
        if (!returnsUiViewController) return reporter.reportOn(declaration.returnTypeRef.source, FirIosApplicationAnnotationDiagnostics.MUST_RETURN_UI_VIEW_CONTROLLER_TYPE)
    }
}