import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.util.internal.MavenUtil

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("common-publishing")
}

description = "Implements 'iosApplication' Declarative Gradle project type"

kotlin {
    explicitApi()
}

dependencies {
    api(gradleApi())
    api(project(":common"))

    implementation(libs.swiftkit.core)
    implementation(libs.jetbrains.compose)
    implementation(libs.compose.compiler)
    compileOnly(libs.jetbrains.compose.runtime)
    compileOnly(libs.kotlin.compiler)
}

commonPublishing {
    configureDefaultJvmPublication()
    extensions.configure<PublishingExtension> {
        publications {
            register("swiftKitCore", MavenPublication::class) {
                val swiftJavaVersion = libs.versions.swift.java.get()
                groupId = "org.swift.swiftkit"
                artifactId = "swiftkit-core"
                version = swiftJavaVersion
                artifact(MavenUtil.getUserMavenDir()
                    .resolve("repository")
                    .resolve("org/swift/swiftkit/swiftkit-core")
                    .resolve("swiftkit-core-$swiftJavaVersion.jar"))
            }
        }
    }
}

val xcodeProjectFormatDir = projectDir.resolve("xcode-project-format")

val applySwiftJavaPatch = tasks.register<Exec>("applySwiftJavaPatch") {
    description = "Applies a git patch with `swift-java` integration to `xcode-project-format` submodule"
    onlyIf("Only possible on macOS platforms") { DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX }

    workingDir = xcodeProjectFormatDir
    commandLine = listOf("bash", "-c", "git apply -R --check --quiet ../swift-java.patch || git apply ../swift-java.patch")
}

val resolveXcodeProjectFormatPackages = tasks.register<Exec>("resolveXcodeProjectParsingPackages") {
    description = "Resolves Swift packages for `xcode-project-format` submodule"
    dependsOn(applySwiftJavaPatch)

    workingDir = xcodeProjectFormatDir
    commandLine = listOf("swift", "package", "resolve")
}

val publishSwiftKitCoreToMavenLocal = tasks.register<Exec>("publishSwiftKitCoreToMavenLocal") {
    description = "Publishes `swiftkit-core` dependency used by `swift-java` to Maven Local"
    dependsOn(resolveXcodeProjectFormatPackages)

    workingDir = xcodeProjectFormatDir.resolve(".build/checkouts/swift-java")
    commandLine = listOf("./gradlew", ":SwiftKitCore:publishToMavenLocal")
}

val buildXcodeProjectFormat = tasks.register<Exec>("buildXcodeProjectFormat") {
    description = "Builds `xcode-project-format` and generate its Java bindings"
    dependsOn(publishSwiftKitCoreToMavenLocal)

    workingDir = xcodeProjectFormatDir
    commandLine = listOf("swift", "build")
}

val copyGeneratedXcodeProjectFormatJavaSources = tasks.register<Copy>("copyGeneratedXcodeProjectFormatJavaSources") {
    description = "Copies the generated Java bindings sources of `xcode-project-format`"
    dependsOn(buildXcodeProjectFormat)

    from(xcodeProjectFormatDir.resolve(".build/plugins/outputs/xcode-project-format")
        .resolve("XcodeProjectFormat/destination/JExtractSwiftPlugin")
        .resolve("src/generated/java"))
    into(layout.buildDirectory.dir("generated/main/java"))
}

val copyBuiltXcodeProjectFormatBinaries = tasks.register<Copy>("copyBuiltXcodeProjectFormatBinaries") {
    description = "Copies the built binaries of `xcode-project-format` and its dependencies"
    dependsOn(buildXcodeProjectFormat)

    val binariesDir = xcodeProjectFormatDir.resolve(".build/debug")
    from(binariesDir.resolve("libSwiftJava.dylib"),
        binariesDir.resolve("libSwiftRuntimeFunctions.dylib"),
        binariesDir.resolve("libXcodeProjectFormat.dylib"))
    into(layout.buildDirectory.dir("generated/main/resources"))
}

val generateXcodeProjectFormatBindings = tasks.register("generateXcodeProjectFormatBindings") {
    group = "build"
    description = "Generates the `xcode-project-format` library Java bindings"
    dependsOn(copyGeneratedXcodeProjectFormatJavaSources)
    dependsOn(copyBuiltXcodeProjectFormatBinaries)
}

tasks.matching { it.name.startsWith("compile") }.configureEach {
    dependsOn(copyGeneratedXcodeProjectFormatJavaSources)
}

tasks.matching { it.name.startsWith("processResources") }.configureEach {
    dependsOn(copyBuiltXcodeProjectFormatBinaries)
}


sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/main/java"))
        }
        resources {
            srcDir(layout.buildDirectory.dir("generated/main/resources"))
        }
    }
}