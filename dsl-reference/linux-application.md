# Linux Application DSL Reference

```kotlin
linuxApplication {
    distributions = listOf(deb, rpm) // Limits the distribution formats for the application to packaged into on Linux platform, defaults to `deb` and `rpm`

    // Additional JVM runtime modules (optional)
    // By default, Compose includes these modules at runtime: "java.base", "java.desktop", "java.logging", "jdk.crypto.ec"
    additionalRuntimeModules = listOf("jdk.unsupported")
    
    kotlin { // Kotlin/JVM compilation configuration
        compilerOptions {
            // KotlinJvmCompilerOptions members are available here, see more at:
            // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-jvm-compiler-options/
        }
    }
    
    packaging {
        name = "..." // The application's name
        version = "<any-format>" // The application's version
        copyright = "..." // The application's copyright information (optional)
        description = "..." // Optional property for the description of the application (optional)
        vendor = "..." // The application's vendor (optional)
        license = layout.projectDirectory.file("...") // The application's license file (optional)

        icon = layout.projectDirectory.file("...") // The application's icon on Linux plaftform (.png) (optional)
        shortcut = true // Enables creating a Linux shortcut upon installation (optional)
        category = "..." // The `Group` value in RPM .spec or `Section` value of DEB control file (optional)
        menuGroup = "..." // The menu group the application is placed in (optional)
        
        debDistribution { // .deb distribution format options (optional)
            version = debVersion("<upstream-version>", <epoch-int> (optional), "<debianRevision>" (optional)) // The application's version for .deb distribution (optional, overrides the version value in `packaging { }`)
            maintainer = "..." // The maintainer for the .deb distribution (optional)
        }

        rpmDistribution { // .rpm distribution format options (optional)
            version = "<any-format>" // The application's version for .rpm distribution (optional, overrides the version value in `packaging { }`)
            licenseType = "..."// The RPM license type (optional)
        }
    }
}
```

## Notes on the BuildModel API for `linuxApplicatiom`

### LinuxApplicationBuildModel (`linuxApplication { ... }`)
Contains the low-level configuration of the (JVM) application:
1. The main class
2. A custom JAVA_HOME
3. Custom JPackage arguments
4. Custom JPackage JVM options
5. Proguard settings of the RELEASE build type