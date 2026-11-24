@file:Suppress("INVISIBLE_REFERENCE")
package org.jetbrains.kotlin.gradle.declarative.xcode

import com.apple.xcode.project.format.XCSchema
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.gradle.declarative.IosApplicationBuildModel
import org.jetbrains.kotlin.gradle.declarative.XcodeProjectGenerationExtension
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.CompileSourcesBuildPhase
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.Configuration
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.FileReference
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.Folder
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.FrameworksBuildPhase
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.Group
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.Project
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.ProjectLocalizationInfo
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.ResourcesBuildPhase
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.ScriptBuildPhase
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asAbbreviatedProductTypeId
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asConfigurationName
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asFileTypeId
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asLanguage
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asLocalTargetReference
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asProductsFilePath
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.asProjectFilePath
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.defaultNativeTargetBuildSettings
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.defaultProjectBuildSettings
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.nativeTarget
import org.jetbrains.kotlin.gradle.declarative.xcode.XCSchema.randomObjectId

context(buildModel: IosApplicationBuildModel)
private val emptyXcodeProject: XCSchema.Project get() =
    Project(
        defaultConfigurationName = "Release".asConfigurationName,
        configurations = listOf(
            Configuration("Release".asConfigurationName),
            Configuration("Debug".asConfigurationName)
        ),
        localizationInfo = ProjectLocalizationInfo(
            development = "en".asLanguage,
            supported = setOf("Base".asLanguage)
        ),
        files = listOf(
            Group(
                name = "Products",
                path = "".asProductsFilePath,
                children = listOf(
                    FileReference(
                        path = buildModel.productName.get().asProductsFilePath,
                        objectID = randomObjectId,
                        explicitFileType = "wrapper.application".asFileTypeId,
                        includeInIndex = false
                    )
                )
            ),
//            FileReference(
//                path = "src/Info.plist".asRawFilePath,
//                textEncoding = XCSchema.TextEncoding.getUtf8()
//            ),
            Folder(
                path = "src".asProjectFilePath,
                targets = setOf("app".asLocalTargetReference),
//                membershipExceptions = listOf(
//                    TargetExceptionSet(
//                        target = "app".asLocalTargetReference,
//                        sense = XCSchema.ExceptionSetSense.exclusions(),
//                        membershipExceptions = setOf("Info.plist".asFolderMemberId)
//                    )
//                )
            )
        ),
        targets = listOf(
            nativeTarget(
                name = "app",
                product = null, // TODO
                productTypeId = "application".asAbbreviatedProductTypeId,
                buildPhases = listOf(
                    CompileSourcesBuildPhase(),
                    FrameworksBuildPhase(),
                    ResourcesBuildPhase(),
                    ScriptBuildPhase(
                        name = "Build Kotlin",
                        logEnvironmentVariables = true,
                        runOnEveryBuild = true,
                        script = """
                            # !KOTLIN INTEGRATION STEP!
                            # This script is managed by the Kotlin Toolchain, do not edit manually!
                            ./gradlew :embedAndSignAppleFrameworkForXcode
                        """.trimIndent()
                    )
                ),
                buildSettings = mutableMapOf<String, XCSchema.BuildSetting>().apply { defaultNativeTargetBuildSettings }
            )
        ),
        buildSettings = mutableMapOf<String, XCSchema.BuildSetting>().apply { defaultProjectBuildSettings }
    )

context(buildModel: IosApplicationBuildModel)
internal fun XcodeProjectGenerationExtension.registerXcodeProjectGenerationTasks(project: Project) {
    val generateEmptyXcodeProject = project.tasks.register("generateEmptyXcodeProject") { task ->
        val projectPath = task.project.projectDir.toPath()
        val xcodeProjectDir = projectPath.resolve("${buildModel.productName.get()}.xcodeproj").toFile()
        task.outputs.dir(xcodeProjectDir)
        task.outputs.upToDateWhen { xcodeProjectDir.exists() }
        task.doLast {
            xcodeProjectDir.mkdir()
            val xcproj = xcodeProjectDir.resolve("project.xcproj")
            xcproj.writeText(emptyXcodeProject.jsonRepresentation().toByteArray().toString(Charsets.UTF_8))
        }
    }
    val buildXcodeProject = project.tasks.register("buildIosApp", Exec::class.java) { task ->
        task.dependsOn(generateEmptyXcodeProject)
        task.commandLine = listOf("xcodebuild", "build", "-project", "${buildModel.productName.get()}.xcodeproj", "CODE_SIGNING_REQUIRED=NO", "CODE_SIGNING_ALLOWED=NO")
    }
    project.tasks.matching { it.name == "build" }.configureEach { task ->
        task.dependsOn(buildXcodeProject)
    }
}