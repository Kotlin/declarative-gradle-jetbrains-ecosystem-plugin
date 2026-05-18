# Library DSL Reference

```dcl
library {
    // Enables platforms library supports
    // Accepted values are LibraryPlatforms names as strings: "jvm", "common", "web", "ios".
    // These are the all platforms supported for now.
    platforms = listOf("common", "jvm", "web", "ios")
    
    kotlin { // Kotlin compilation configuration common for all platforms
         compilerOptions { // Compiler arguments to apply for all Kotlin compilations
             // KotlinCommonCompilerOptions members are available here, see more at:
             // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-common-compiler-options/
             // (for example: languageVersion, apiVersion, freeCompilerArgs, etc.)
         }

        serialization { // Enables Kotlin serialization compiler plugins
            version = "1.11.0" // Version of kotlinx.serialization dependencies to add into the project, default is 1.11.0 
            // Serialization format dependencies to add
            // Accepted values are "json", "protobuf", "cbor", "hocon", "properties"
            enabledFormats = listOf("json")
        }
    }
    
    java { // Java compilation configuration common for all platforms
        compilerOptions { // compiler arguments to apply for all Java compilations
            compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
        }
    }

    jvmPlatform { // JVM platform specific configurations
        toolchain { // JDK toolchain which is used both for compilation and test tasks
            releaseVersion = 21 // JDK release version
            vendor = JvmVendor.ADOPTIUM // JDK vendor
            nativeImageCapable = false // Indicates if the JDK should be native-image capable
        }
        
        kotlin { // Kotlin JVM compilation configuration, overrides common configuration
            compilerOptions { // Compiler arguments to apply for all Kotlin JVM compilations
                // KotlinJvmCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-jvm-compiler-options/
            }
        }
    }

    webPlatform { // Web platform specific configurations
        // Limits "web" platform to given sub-platforms
        // Accepted values are WebSubplatforms names as strings: "js", "wasmJs".
        // If omitted (or empty), all subplatforms are enabled by default.
        subplatforms = listOf("js", "wasmJs")

        kotlin { // Kotlin compilation configuration, overrides common configuration
            compilerOptions {
                // KotlinJsCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-js-compiler-options/
                // (for example: sourceMap, target, etc.)
            }
        }
    }

    iosPlatform { // iOS platform specific configurations
        // Limits "ios" platform to given sub-platforms
        // Accepted values are IosSubplatforms names as strings: "iosArm64", "iosSimulatorArm64", "iosX64".
        // If omitted (or empty), all iOS subplatforms are enabled by default.
        subplatforms = listOf("iosArm64", "iosSimulatorArm64", "iosX64")

        kotlin { // Kotlin compilation configuration, overrides common configuration
            compilerOptions {
                // KotlinNativeCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-native-compiler-options/
            }
        }
    }

    dependencies { // library dependencies
        api(project(":shared")) // common dependency for all platforms
        implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0") // common dependency fro all platforms

        jvmPlatform { // JVM specific dependencies
            api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
            implementation("org.jetbrains.kotlin:kotlin-reflect")
            compileOnly("org.jspecify:jspecify:1.0.0")
            runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
        }

        webPlatform { // Web specific dependencies
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }

        iosPlatformDependencies { // IOS specific dependencies
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
    }

    testing { // Library testing configuration
        jvmPlatform { // Testing configuration for JVM platform 
            useJunitPlatform = true // Enable JUnit5 platform for tests
        }

        webPlatform { // Testing configuration for Web platform
            skip = true // Skip web tests
        }

        dependencies { // Testing dependencies
            implementation("org.jetbrains.kotlin:kotlin-test") // Common testing for all platforms 

            jvmPlatform { // JVM platform testing dependencies
                implementation("org.junit.jupiter:junit-jupiter:5.12.2")
                compileOnly("org.jetbrains:annotations:26.0.2")
                runtimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")

                // test fixtures modifiers are also available here
                implementation(testFixtures(project(":shared")))
            }

            webPlatform { // Web platform testing dependencies
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
            }

            iosPlatformDependencies { // IOS platform testing dependencies
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }

    publishing { // Publishing configuration
        group = "com.example" // Publication group
        version = "1.0.0" // Publication name
        
        maven { // Enables Gradle 'maven-publish' plugina and configures publication to Maven
           name = "library" // Publication name
           repositoryUrl = "file:///tmp" // Publication repository url
           withDocs = true // Enable documentation publication
           withSources = true // Enable sources publication
        }
    }
}
```

## Examples

- [examples/idea-wizard/utils/build.gradle.dcl](../examples/idea-wizard/utils/build.gradle.dcl)
- [examples/spring-petklinik/shared/build.gradle.dcl](../examples/spring-petklinik/shared/build.gradle.dcl)
