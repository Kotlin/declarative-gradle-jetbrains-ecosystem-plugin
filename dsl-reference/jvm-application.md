# JVM Application DSL Reference

```dcl
jvmApplication {
    mainClass = "com.example.app.MainKt" // Application main class
    name = "example-app" // Application name
    moduleName = "com.example.app" // The name of the application's Java module if it should run as a module
    jvmArgs = listOf("-Xmx512m", "-Dfile.encoding=UTF-8") // Application JVM arguments


    toolchain { // JDK toolchain which is used both for compilation and test tasks
        releaseVersion = 21 // JDK release version
        vendor = JvmVendor.ADOPTIUM // JDK vendor
        nativeImageCapable = false // Indicates if the JDK should be native-image capable
    }

    java { // Java compilation configuration
        compilerOptions { // compiler arguments to apply for all Java compilations
            compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
        }
    }

    kotlin { // Kotlin compilation configuration
        compilerOptions { // Compiler arguments to apply for all Kotlin compilations
            // KotlinJvmCompilerOptions members are also available here, see more at:
            // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-jvm-compiler-options/
            // (for example: languageVersion, apiVersion, jvmTarget, freeCompilerArgs, etc.)
        }
        
        serialization { // Enables Kotlin serialization compiler plugins
            version = "1.11.0" // Version of kotlinx.serialization dependencies to add into the project, default is 1.11.0 
            // Serialization format dependencies to add
            // Accepted values are "json", "protobuf", "cbor", "hocon", "properties"
            enabledFormats = listOf("json")
        }
    }

    spring { } // Enables Spring framework support by applying "org.springframework.boot" plugin and adding Spring bom to the dependencies

    dependencies { // Application dependencies
        implementation(project(":shared"))
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        compileOnly("org.jspecify:jspecify:1.0.0")
        runtimeOnly("org.postgresql:postgresql")

        // It is possible exclude some transtive dependencies
        implementation("org.springframework.boot:spring-boot-starter-webmvc") {
            exclude(mapOf("group" to "org.springframework.boot", "module" to "spring-boot-starter-jackson"))
        }
        
        spring { // Spring specific dependencies, requires jvmApplication.spring {} to be applied
           developmentOnly("org.springframework.boot:spring-boot-docker-compose")
        }
    }

    testing { // Application testing configuration
        useJUnitPlatform = true // Enable JUnit5 platform for tests

        dependencies { // testing dependencies
            implementation("org.junit.jupiter:junit-jupiter:5.12.2")
            compileOnly("org.jetbrains:annotations:26.0.2")
            runtimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")

            // test fixtures modifiers are also available here
            implementation(testFixtures(project(":shared")))
        }
    }

    packaging { // Application packaging configuration
        distribution { // Enables Gradle distribution plugin: https://docs.gradle.org/current/userguide/distribution_plugin.html
           name = "main" // The name of distribution
           classifier = "" // The classifier of the distribution, used as the archive classifier in the archives of this distribution
        }
        
        spring { // Spring specific packaging configuration
           bootBuildImage { // Configures Spring "bootBuildImage" task
               environment += mapOf(
                    "BP_JVM_CDS_ENABLED" to "true",
                    "BP_JVM_VERSION" to "25"
               )
           }
        }
        
        resource { } // Expose this assembled Application to another project to consume it as resource
    }
}
```

## Examples

- [examples/idea-wizard/app/build.gradle.dcl](../examples/idea-wizard/app/build.gradle.dcl)
- [examples/spring-petklinik/backend/build.gradle.dcl](../examples/spring-petklinik/backend/build.gradle.dcl)
