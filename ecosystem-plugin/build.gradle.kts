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
