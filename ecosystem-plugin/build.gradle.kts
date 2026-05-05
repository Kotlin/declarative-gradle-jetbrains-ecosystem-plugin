plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    id("common-publishing")
}

kotlin {
    explicitApi()
}

dependencies {
    api(project(":project-types:jvm-application"))
    api(project(":project-types:web-application"))
    api(project(":project-types:library"))
    api(project(":spring"))
    api(project(":resource-packaging"))

    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("jetbrains-ecosystem-plugin") {
            id = "org.jetbrains.ecosystem"
            displayName = "JetBrains Ecosystem Declarative Plugin"
            description = "Experimental declarative plugin for JetBrains (including Kotlin) ecosystem"
            implementationClass = "org.jetbrains.kotlin.gradle.declarative.JetbrainsEcosystemPlugin"
            tags = setOf("declarative-gradle", "kotlin", "kotlin-multiplatform")
        }
    }
}

testing {
    suites {
        @Suppress("UnstableApiUsage")
        register<JvmTestSuite>("integrationTests") {
            dependencies {
                implementation(gradleTestKit())
                implementation(libs.kotlin.gradle.plugin.api)
                implementation(libs.kotlin.test)
            }

            targets {
                all {
                    testTask.configure {
                        dependsOn("publishJetbrains-ecosystem-pluginPluginMarkerMavenPublicationToRepoLocalRepository")
                        dependsOn("publishPluginMavenPublicationToRepoLocalRepository")
                        dependsOn(":project-types:jvm-application:publishMavenPublicationToRepoLocalRepository")
                        dependsOn(":project-types:web-application:publishMavenPublicationToRepoLocalRepository")
                        dependsOn(":project-types:library:publishMavenPublicationToRepoLocalRepository")
                        dependsOn(":common:publishMavenPublicationToRepoLocalRepository")
                        dependsOn(":spring:publishMavenPublicationToRepoLocalRepository")
                        dependsOn(":resource-packaging:publishMavenPublicationToRepoLocalRepository")

                        javaLauncher
                            .value(jdkLauncherFor(17))
                            .disallowChanges()

                        val jdk8Provider = jdkToolchainHomeFor(8)
                        val jdk11Provider = jdkToolchainHomeFor(11)
                        val jdk17Provider = jdkToolchainHomeFor(17)
                        val jdk21Provider = jdkToolchainHomeFor(21)

                        val publishedRepo = project.extensions.getByType<CommonPublishingExtension>().publishedRepo

                        doFirst {
                            systemProperty("jdk8Home", jdk8Provider.get())
                            systemProperty("jdk11Home", jdk11Provider.get())
                            systemProperty("jdk17Home", jdk17Provider.get())
                            systemProperty("jdk21Home", jdk21Provider.get())
                            systemProperty(
                                "maven.repo.url",
                                publishedRepo.absolutePath
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Project.jdkLauncherFor(jdkVersion: Int): Provider<JavaLauncher> {
    val jdkToolchainService = extensions.getByType<JavaToolchainService>()
    return jdkToolchainService.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(jdkVersion))
    }
}

fun Project.jdkToolchainHomeFor(jdkVersion: Int): Provider<String> {
    return jdkLauncherFor(jdkVersion).map {
        it.metadata.installationPath.asFile.absolutePath
    }
}
