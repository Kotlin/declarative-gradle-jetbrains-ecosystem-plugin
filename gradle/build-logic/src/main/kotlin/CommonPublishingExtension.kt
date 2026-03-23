import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import javax.inject.Inject

abstract class CommonPublishingExtension @Inject constructor(
    private val project: Project
) {
    val publishedRepo = project.rootProject.layout.buildDirectory.dir("repo")

    fun configureDefaultJvmPublication() {
        project.extensions.configure<PublishingExtension>() {
            publications {
                register("maven", MavenPublication::class.java) {
                    from(project.components["java"])
                }
            }
        }
    }
}
