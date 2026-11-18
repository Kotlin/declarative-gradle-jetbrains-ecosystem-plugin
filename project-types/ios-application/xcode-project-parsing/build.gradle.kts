plugins {
    `kotlin-dsl`
    `maven-publish`
}

dependencies {
    implementation(":build-logic:+")
}

gradlePlugin {
    plugins {
        create("xcode-project-parsing") {
            id = "xcode-project-parsing"
            implementationClass = "XcodeProjectParsingPlugin"
        }
    }
}