import org.gradle.api.Project
import javax.inject.Inject

abstract class CommonPublishingExtension @Inject constructor(
    private val project: Project
) {
    val publishedRepo = project.rootProject.layout.buildDirectory.dir("repo")
}