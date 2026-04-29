import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure

plugins {
    `maven-publish`
}

group = "org.jetbrains.ecosystem"
version = providers.gradleProperty("projectVersion").orElse("0.0.1-SNAPSHOT").get()

val extension = extensions.create<CommonPublishingExtension>("commonPublishing")

val githubActor = providers.gradleProperty("githubPackagesUsername")
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
val githubToken = providers.gradleProperty("githubPackagesToken")
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            url = uri(extension.publishedRepo)
            name = "RepoLocal"
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin")
            credentials {
                username = githubActor.orNull
                password = githubToken.orNull
            }
        }
    }
}