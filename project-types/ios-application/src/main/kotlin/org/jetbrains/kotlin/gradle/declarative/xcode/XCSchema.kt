package org.jetbrains.kotlin.gradle.declarative.xcode

import com.apple.xcode.project.format.XCSchema
import org.jetbrains.kotlin.gradle.declarative.IosApplicationBuildModel
import java.security.MessageDigest
import java.util.UUID

internal object XCSchema {

    inline val randomObjectId: XCSchema.ObjectID get() {
        val messageDigest = MessageDigest.getInstance("MD5")
        val reference = messageDigest.digest(
            UUID.randomUUID().toString().toByteArray()
        ).joinToString(separator = "") { byte -> "%02x".format(byte) }.uppercase().subSequence(0, 24).toString()
        return XCSchema.ObjectID.init(reference)
    }

    inline val String.asConfigurationName: XCSchema.ConfigurationName get() = XCSchema.ConfigurationName.initName(this)

    inline val String.asLanguage: XCSchema.Language get() = XCSchema.Language.initLanguageID(this)

    inline val String.asProductTypeId: XCSchema.ProductTypeID get() = XCSchema.ProductTypeID.initProductTypeID(this)

    inline val String.asAbbreviatedProductTypeId: XCSchema.ProductTypeID get() =
        XCSchema.ProductTypeID.initAbbreviatedRepresentation(this)

    inline val String.asRawFilePath: XCSchema.FilePath get() = XCSchema.FilePath.init(this)

    inline val String.asProjectFilePath: XCSchema.FilePath get() =
        XCSchema.FilePath.init(XCSchema.FilePath.Base.project(), this)

    inline val String.asProductsFilePath: XCSchema.FilePath get() =
        XCSchema.FilePath.init(XCSchema.FilePath.Base.buildProducts(), this)

    inline val String.asLocalTargetReference: XCSchema.LocalTargetReference get() =
        XCSchema.LocalTargetReference.initTargetName(this)

    inline val String.asFileTypeId: XCSchema.FileTypeID get() = XCSchema.FileTypeID.initFileTypeID(this)

    inline val String.asFolderMemberId: XCSchema.FolderMemberID get() = XCSchema.FolderMemberID.initValue(this)

    fun Configuration(
        name: XCSchema.ConfigurationName,
        file: XCSchema.GroupTreeAnchoredReference? = null,
        objectID: XCSchema.ObjectID? = null,
    ) : XCSchema.Configuration = XCSchema.Configuration.init(name, file.optional, objectID.optional)

    fun ProjectLocalizationInfo(
        development: XCSchema.Language,
        supported: Set<XCSchema.Language>
    ) : XCSchema.ProjectLocalizationInfo = XCSchema.ProjectLocalizationInfo.init(
        development,
        supported.toSwiftSet()
    )

    fun CompileSourcesBuildPhase(
        objectID: XCSchema.ObjectID? = null,
        name: String? = null,
    ): XCSchema.BuildPhase {
        val properties = XCSchema.BuildPhaseProperties.init(objectID.optional, name.optional)
        return XCSchema.BuildPhase.sources(properties)
    }

    fun FrameworksBuildPhase(
        objectID: XCSchema.ObjectID? = null,
        name: String? = null,
    ): XCSchema.BuildPhase {
        val properties = XCSchema.BuildPhaseProperties.init(objectID.optional, name.optional)
        return XCSchema.BuildPhase.frameworks(properties)
    }

    fun ResourcesBuildPhase(
        objectID: XCSchema.ObjectID? = null,
        name: String? = null,
    ): XCSchema.BuildPhase {
        val properties = XCSchema.BuildPhaseProperties.init(objectID.optional, name.optional)
        return XCSchema.BuildPhase.resources(properties)
    }

    fun ScriptBuildPhase(
        objectID: XCSchema.ObjectID? = null,
        name: String,
        shellPath: String = "/bin/sh",
        script: String,
        logEnvironmentVariables: Boolean = false,
        inputPaths: List<String> = emptyList(),
        inputFilePaths: List<String> = emptyList(),
        outputPaths: List<String> = emptyList(),
        outputFilePaths: List<String> = emptyList(),
        dependencyFile: String? = null,
        runOnEveryBuild: Boolean = false,
        scope: XCSchema.BuildPhaseScope = XCSchema.BuildPhaseScope.always()
    ): XCSchema.BuildPhase {
        val properties = XCSchema.ScriptBuildPhaseProperties.init(
            objectID.optional,
            name,
            shellPath,
            script,
            logEnvironmentVariables,
            inputPaths.toTypedArray(),
            inputFilePaths.toTypedArray(),
            outputPaths.toTypedArray(),
            outputFilePaths.toTypedArray(),
            dependencyFile.optional,
            runOnEveryBuild,
            scope
        )
        return XCSchema.BuildPhase.script(properties)
    }

    fun MutableMap<String, XCSchema.BuildSetting>.put(name: String, value: String) = put(name, XCSchema.BuildSetting.string(value))

    fun MutableMap<String, XCSchema.BuildSetting>.put(name: String, vararg values: String) = put(name, XCSchema.BuildSetting.array(values))

    context(buildModel: IosApplicationBuildModel)
    inline val MutableMap<String, XCSchema.BuildSetting>.defaultNativeTargetBuildSettings: Unit get() {
//        put("ASSETCATALOG_COMPILER_APPICON_NAME", "AppIcon")
        put("CODE_SIGN_IDENTITY", "Apple Development")
        put("CODE_SIGN_STYLE", "Automatic")
        put("CURRENT_PROJECT_VERSION", "1")
        put("DEVELOPMENT_TEAM", "bleh")
        put("ENABLE_PREVIEWS", "YES")
        // TODO: provide Info.plist file?
//        put("INFOPLIST_FILE", "src/Info.plist")
        put("LD_RUNPATH_SEARCH_PATHS",
            "$(inherited)",
            "@executable_path/Frameworks",
            "@loader_path/Frameworks",
        )
        put("MARKETING_VERSION", "1.0")
        put("PRODUCT_BUNDLE_IDENTIFIER", buildModel.productBundleIdentifier.get())
        put("PRODUCT_MODULE_NAME", "app")
        put("PRODUCT_NAME", buildModel.productName.get())
        put("SDKROOT", "iphoneos")
        put("SWIFT_APPROACHABLE_CONCURRENCY", "YES")
        put("SWIFT_DEFAULT_ACTOR_ISOLATION", "MainActor")
        put("SWIFT_EMIT_LOC_STRINGS", "YES")
        put("SWIFT_UPCOMING_FEATURE_MEMBER_IMPORT_VISIBILITY", "YES")
        put("SWIFT_VERSION", "5.0")
        put("TARGETED_DEVICE_FAMILY", "1 2")
        put("VALIDATE_PRODUCT[config=Release]", "YES")
    }

    fun nativeTarget(
        name: String,
        objectID: XCSchema.ObjectID = randomObjectId,
        configurationListDebugID: XCSchema.ObjectID? = null,
        dependencies: List<XCSchema.TargetDependency> = emptyList(),
        buildPhases: List<XCSchema.BuildPhase>,
        buildRules: List<XCSchema.BuildRule> = emptyList(),
        specializedConfigurations: List<XCSchema.Configuration> = emptyList(),
        buildSettings: Map<String, XCSchema.BuildSetting>,
        product: XCSchema.GroupTreeReference? = null,
        productTypeId: XCSchema.ProductTypeID? = null,
        testHostTarget: XCSchema.LocalTargetReference? = null,
        legacyProvisioningStyle: XCSchema.LegacyProvisioningStyle? = null,
        legacyTeamId: String? = null,
        lastSwiftUpdateCheck: XCSchema.MarketingVersion? = null,
        lastSwiftMigration: XCSchema.MarketingVersion? = null,
        packageProductTargetMembers: List<XCSchema.SwiftPackageProductTargetMember> = emptyList()
    ) : XCSchema.Target {
        val properties = XCSchema.CommonTargetProperties.init(
            name,
            objectID,
            configurationListDebugID.optional,
            dependencies.toTypedArray(),
            buildPhases.toTypedArray(),
            buildRules.toTypedArray(),
            specializedConfigurations.toTypedArray(),
            buildSettings.toSwiftDictionary(),
            product.optional,
            productTypeId.optional,
            testHostTarget.optional,
            legacyProvisioningStyle.optional,
            legacyTeamId.optional,
            lastSwiftUpdateCheck.optional,
            lastSwiftMigration.optional,
            packageProductTargetMembers.toTypedArray(),
        )
        return XCSchema.Target.native_(properties)
    }

    inline val MutableMap<String, XCSchema.BuildSetting>.defaultProjectBuildSettings: Unit get() {
        put("ALWAYS_SEARCH_USER_PATHS", "NO")
        put("ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS", "YES")
        put("CLANG_ANALYZER_NONNULL", "YES")
        put("CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION", "YES_AGGRESSIVE")
        put("CLANG_CXX_LANGUAGE_STANDARD", "gnu++20")
        put("CLANG_ENABLE_MODULES", "YES")
        put("CLANG_ENABLE_OBJC_ARC", "YES")
        put("CLANG_ENABLE_OBJC_WEAK", "YES")
        put("CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING", "YES")
        put("CLANG_WARN_BOOL_CONVERSION", "YES")
        put("CLANG_WARN_COMMA", "YES")
        put("CLANG_WARN_CONSTANT_CONVERSION", "YES")
        put("CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS", "YES")
        put("CLANG_WARN_DIRECT_OBJC_ISA_USAGE", "YES_ERROR")
        put("CLANG_WARN_DOCUMENTATION_COMMENTS", "YES")
        put("CLANG_WARN_EMPTY_BODY", "YES")
        put("CLANG_WARN_ENUM_CONVERSION", "YES")
        put("CLANG_WARN_INFINITE_RECURSION", "YES")
        put("CLANG_WARN_INT_CONVERSION", "YES")
        put("CLANG_WARN_NON_LITERAL_NULL_CONVERSION", "YES")
        put("CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF", "YES")
        put("CLANG_WARN_OBJC_LITERAL_CONVERSION", "YES")
        put("CLANG_WARN_OBJC_ROOT_CLASS", "YES_ERROR")
        put("CLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER", "YES")
        put("CLANG_WARN_RANGE_LOOP_ANALYSIS", "YES")
        put("CLANG_WARN_STRICT_PROTOTYPES", "YES")
        put("CLANG_WARN_SUSPICIOUS_MOVE", "YES")
        put("CLANG_WARN_UNGUARDED_AVAILABILITY", "YES_AGGRESSIVE")
        put("CLANG_WARN_UNREACHABLE_CODE", "YES")
        put("CLANG_WARN__DUPLICATE_METHOD_MATCH", "YES")
        put("COPY_PHASE_STRIP", "NO")
        put("DEAD_CODE_STRIPPING", "YES")
        put("DEBUG_INFORMATION_FORMAT[config=Debug]", "dwarf")
        put("DEBUG_INFORMATION_FORMAT[config=Release]", "dwarf-with-dsym")
        put("ENABLE_NS_ASSERTIONS[config=Release]", "NO")
        put("ENABLE_STRICT_OBJC_MSGSEND", "YES")
        put("ENABLE_TESTABILITY[config=Debug]", "YES")
        put("ENABLE_USER_SCRIPT_SANDBOXING", "NO")
        put("GCC_C_LANGUAGE_STANDARD", "gnu17")
        put("GCC_DYNAMIC_NO_PIC[config=Debug]", "NO")
        put("GCC_NO_COMMON_BLOCKS", "YES")
        put("GCC_OPTIMIZATION_LEVEL[config=Debug]", "0")
        put("GCC_PREPROCESSOR_DEFINITIONS[config=Debug]",
            "DEBUG=1",
            "$(inherited)",
        )
        put("GCC_WARN_64_TO_32_BIT_CONVERSION", "YES")
        put("GCC_WARN_ABOUT_RETURN_TYPE", "YES_ERROR")
        put("GCC_WARN_UNDECLARED_SELECTOR", "YES")
        put("GCC_WARN_UNINITIALIZED_AUTOS", "YES_AGGRESSIVE")
        put("GCC_WARN_UNUSED_FUNCTION", "YES")
        put("GCC_WARN_UNUSED_VARIABLE", "YES")
        put("IPHONEOS_DEPLOYMENT_TARGET", "18.2")
        put("LOCALIZATION_PREFERS_STRING_CATALOGS", "YES")
        put("MTL_ENABLE_DEBUG_INFO[config=Debug]", "INCLUDE_SOURCE")
        put("MTL_ENABLE_DEBUG_INFO[config=Release]", "NO")
        put("MTL_FAST_MATH", "YES")
        put("ONLY_ACTIVE_ARCH[config=Debug]", "YES")
        put("STRING_CATALOG_GENERATE_SYMBOLS", "YES")
        put("SWIFT_ACTIVE_COMPILATION_CONDITIONS[config=Debug]", "DEBUG $(inherited)")
        put("SWIFT_COMPILATION_MODE[config=Release]", "wholemodule")
        put("SWIFT_OPTIMIZATION_LEVEL[config=Debug]", "-Onone")
    }

    fun TargetExceptionSet(
        target: XCSchema.LocalTargetReference,
        publicHeaders: Set<XCSchema.FolderMemberID> = emptySet(),
        privateHeaders: Set<XCSchema.FolderMemberID> = emptySet(),
        additionalCompilerFlags: Map<XCSchema.FolderMemberID, String> = emptyMap(),
        sense: XCSchema.ExceptionSetSense,
        membershipExceptions: Set<XCSchema.FolderMemberID>,
        platformFiltersByFolderMemberID: Map<XCSchema.FolderMemberID, Set<XCSchema.PlatformFilter>> = emptyMap(),
        attributesByFolderMemberID: Map<XCSchema.FolderMemberID, XCSchema.BuildFileAttributes> = emptyMap(),
        assetTagsByFolderMemberID: Map<XCSchema.FolderMemberID, Set<XCSchema.AssetTag>> = emptyMap(),
    ): XCSchema.FolderExceptionSet {
        val commonProperties = XCSchema.CommonExceptionSetProperties.init(
            sense,
            membershipExceptions.toSwiftSet(),
            platformFiltersByFolderMemberID.toSwiftDictionary(),
            attributesByFolderMemberID.toSwiftDictionary(),
            assetTagsByFolderMemberID.toSwiftDictionary(),
        )
        val targetExceptionSet = XCSchema.TargetExceptionSet.init(
            target,
            publicHeaders.toSwiftSet(),
            privateHeaders.toSwiftSet(),
            additionalCompilerFlags.toSwiftDictionary(),
            commonProperties
        )
        return XCSchema.FolderExceptionSet.target(targetExceptionSet)
    }

    fun Group(
        objectID: XCSchema.ObjectID? = null,
        name: String,
        path: XCSchema.FilePath,
        includeInIndex: Boolean? = null,
        children: List<XCSchema.Reference>,
    ): XCSchema.Reference {
        val group = XCSchema.Group.init(
            objectID.optional,
            name,
            path,
            includeInIndex.optional,
            children.toTypedArray(),
        )
        return XCSchema.Reference.group(group)
    }

    fun FileReference(
        objectID: XCSchema.ObjectID? = null,
        path: XCSchema.FilePath,
        explicitFileType: XCSchema.FileTypeID? = null,
        expectedSignature: String? = null,
        textEncoding: XCSchema.TextEncoding? = null,
        lineEnding: XCSchema.LineEnding? = null,
        includeInIndex: Boolean? = null,
        buildFiles: List<XCSchema.ProjectBuildFile> = emptyList()
    ): XCSchema.Reference {
        val fileReference = XCSchema.FileReference.init(
            objectID.optional,
            path,
            explicitFileType.optional,
            expectedSignature.optional,
            textEncoding.optional,
            lineEnding.optional,
            includeInIndex.optional,
            buildFiles.toTypedArray()
        )
        return XCSchema.Reference.fileReference(fileReference)
    }

    fun Folder(
        objectID: XCSchema.ObjectID? = null,
        path: XCSchema.FilePath,
        targets: Set<XCSchema.LocalTargetReference>,
        membershipExceptions: List<XCSchema.FolderExceptionSet> = emptyList(),
        explicitFileTypes: Map<XCSchema.FolderMemberID, XCSchema.FileTypeID> = emptyMap(),
        explicitOpaqueFolders: Set<XCSchema.FolderMemberID> = emptySet(),
        includeInIndex: Boolean? = null,
    ): XCSchema.Reference {
        val folder = XCSchema.Folder.init(
            objectID.optional,
            path,
            targets.toSwiftSet(),
            membershipExceptions.toTypedArray(),
            explicitFileTypes.toSwiftDictionary(),
            explicitOpaqueFolders.toSwiftSet(),
            includeInIndex.optional,
        )
        return XCSchema.Reference.folder(folder)
    }

    fun Project(
        objectID: XCSchema.ObjectID? = null,
        rootGroupDebugID: XCSchema.ObjectID? = null,
        configurationListDebugID: XCSchema.ObjectID? = null,
        files: List<XCSchema.Reference>,
        packages: List<XCSchema.SwiftPackage> = emptyList(),
        configurations: List<XCSchema.Configuration>,
        buildSettings: Map<String, XCSchema.BuildSetting> = emptyMap(),
        defaultConfigurationName: XCSchema.ConfigurationName,
        targets: List<XCSchema.Target>,
        localizationInfo: XCSchema.ProjectLocalizationInfo,
        requiredCapabilities: Set<XCSchema.Capability> = emptySet(),
        buildIndependentTargetsInParallel: Boolean = false,
        lastUpgradeCheck: XCSchema.MarketingVersion? = null,
        lastSwiftUpdateCheck: XCSchema.MarketingVersion? = null,
        lastSwiftMigration: XCSchema.MarketingVersion? = null,
        organizationName: String? = null,
        classPrefix: String? = null,
        productsGroup: XCSchema.GroupTreeReference? = null,
        importedProducts: List<XCSchema.RemoteProduct> = emptyList()
    ) : XCSchema.Project = XCSchema.Project.init(
        objectID.optional,
        rootGroupDebugID.optional,
        configurationListDebugID.optional,
        files.toTypedArray(),
        packages.toTypedArray(),
        configurations.toTypedArray(),
        buildSettings.toSwiftDictionary(),
        defaultConfigurationName,
        targets.toTypedArray(),
        localizationInfo,
        requiredCapabilities.toSwiftSet(),
        buildIndependentTargetsInParallel,
        lastUpgradeCheck.optional,
        lastSwiftUpdateCheck.optional,
        lastSwiftMigration.optional,
        organizationName.optional,
        classPrefix.optional,
        productsGroup.optional,
        importedProducts.toTypedArray()
    )
}