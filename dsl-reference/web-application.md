# Web Application DSL Reference

```dcl
webApplication {
    kotlin { // Kotlin compilation configuration
        compilerOptions { // Compiler arguments to  apply for all Kotlin compilations
            // KotlinJsCompilerOptions members are available here, see more at:
            // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-js-compiler-options/
            // (for example: moduleName, sourceMap, freeCompilerArgs, target, etc.)
        }
        
        serialization { // Enables Kotlin serialization compiler plugins
            version = "1.11.0" // Version of kotlinx.serialization dependencies to add into the project, default is 1.11.0 
            // Serialization format dependencies to add
            // Accepted values are "json", "protobuf", "cbor", "hocon", "properties"
            enabledFormats = listOf("json")
        }
    }

    // Limits "web" platform to given sub-platforms
    // Accepted values are WebSubplatforms names as strings: "js", "wasmJs"
    // If omitted (or empty), all subplatforms are enabled by default
    subplatforms = listOf("js", "wasmJs")

    dependencies { // Web application dependencies
        implementation(project(":shared"))
        implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
    }

    packaging { // Application packaging configuration
        resource { } // Expose this assembled Application to another project to consume it as resource
    }    
}
```

## Examples

- [examples/spring-petklinik/frontend/build.gradle.dcl](../examples/spring-petklinik/frontend/build.gradle.dcl)
