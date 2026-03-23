plugins {
    `maven-publish`
}

group = "org.jetbrains.ecosystem"
version = "0.0.1-SNAPSHOT"

val extension = extensions.create<CommonPublishingExtension>("commonPublishing")
publishing {
    repositories {
        maven {
            url = uri(extension.publishedRepo)
            name = "RepoLocal"
        }
    }
}