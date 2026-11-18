import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.PluginManager
import org.gradle.internal.SystemProperties
import org.gradle.kotlin.dsl.getByType
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject
import kotlin.io.path.toPath

abstract class XcodeProjectParsingPlugin : Plugin<Project> {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val pluginManager: PluginManager

    override fun apply(target: Project) {
        pluginManager.withPlugin("common-publishing") {
            target.afterEvaluate {
                val publishedRepo = extensions.getByType<CommonPublishingExtension>().publishedRepo

                val pbxproj = rootDir.resolve("project-types/ios-application/xcode-project-parsing/pbxproj")
                val xcproj = rootDir.resolve("project-types/ios-application/xcode-project-parsing/xcproj")
                val xcodeProjectFile = rootDir.resolve("project-types/ios-application/xcode-project-parsing/xcode-project-file")
                val kotlinToolchainProjects = listOf(pbxproj, xcproj, xcodeProjectFile)

                kotlinToolchainProjects.forEach {
                    val artifactName = "${it.name}-1.0-SNAPSHOT"
                    val pomFileName = "$artifactName.pom"
                    val artifactPath = "org/jetbrains/${it.name}/1.0-SNAPSHOT/$pomFileName"

                    val mavenLocalArtifact = File(SystemProperties.getInstance().userHome, ".m2/repository").resolve(artifactPath)
                    when {
                        mavenLocalArtifact.exists() -> Unit
                        else -> {
                            logger.lifecycle(mavenLocalArtifact.absolutePath)
                            val mavenLocalPublication = execOperations.exec {
                                workingDir = it
                                logger.lifecycle("Publishing Kotlin Toolchain project: $it, (repo: Maven Local)")
                                when {
                                    it == xcproj -> commandLine("./kotlin", "publish", "-m", "xcproj", "mavenLocal")
                                    else -> commandLine("./kotlin", "publish", "mavenLocal")
                                }
                                target.logging.captureStandardOutput(LogLevel.LIFECYCLE)
                            }.exitValue
                            assert(mavenLocalPublication == 0) { "Publishing artifact failed to Maven Local: $mavenLocalPublication" }
                        }
                    }

                    val publishedRepoArtifact = publishedRepo.resolve(artifactPath)
                    when {
                        publishedRepoArtifact.exists() -> Unit
                        else -> {
                            logger.lifecycle(publishedRepoArtifact.absolutePath)
                            val publishedRepoPublication = execOperations.exec {
                                workingDir = it
                                logger.lifecycle("Publishing Kotlin Toolchain project: $it, (repo: ${publishedRepo.name})")
                                environment("KOTLIN_CLI_JAVA_OPTIONS", "-Dmaven.repo.local=$publishedRepo")
                                when {
                                    it == xcproj -> commandLine("./kotlin", "publish", "-m", "xcproj", "mavenLocal")
                                    else -> commandLine("./kotlin", "publish", "mavenLocal")
                                }
                                target.logging.captureStandardOutput(LogLevel.LIFECYCLE)
                            }.exitValue

                            assert(publishedRepoPublication == 0) { "Publishing artifact failed to $publishedRepo: $publishedRepoPublication" }
                        }
                    }
                }
            }
        }
    }
}