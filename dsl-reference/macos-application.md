# macOS Application DSL Reference

```kotlin
macosApplication {
    distributions = listOf(dmg, pkg) // Limits the distribution formats for the application to packaged into on macOS platform, defaults to `dmg` and `pkg`

    // Additional JVM runtime modules (optional)
    // By default, Compose includes these modules at runtime: "java.base", "java.desktop", "java.logging", "jdk.crypto.ec"
    additionalRuntimeModules = listOf("jdk.unsupported")
    minimumMacosVersion = "..." // The minimum macOS system version required to run the application (optional)

    kotlin { // Kotlin/JVM compilation configuration
        compilerOptions {
            // KotlinJvmCompilerOptions members are available here, see more at:
            // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-jvm-compiler-options/
        }
    }
    
    packaging { // Application packaging configuration
        name = "..." // The application's name
        version = version(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's version
        copyright = "..." // The application's copyright information (optional)
        description = "..." // Optional property for the description of the application (optional)
        vendor = "..." // The application's vendor (optional)
        license = layout.projectDirectory.file("...") // The application's license file (optional)

        icon = layout.projectDirectory.file("...") // The application's icon on macOS plaftform (.icns) (optional)
        buildVersion = version(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's (build) version visible to the system (optional, defaults to the application's version)
        // The category of the app in App Store (see more https://developer.apple.com/documentation/bundleresources/information-property-list/lsapplicationcategorytype) (optional, default is utilities)
        // The property will be modelled as enum instead of raw strings in the format "public.app-category.<category>"
        category = utilities
        
        signing { // The signing options for macOS distribution (optional)
            enable = true // Enables signing for declared macOS distribution
            signForAppStore = true // Enables the building and signing of the application for Apple App Store
            identity = "..." // The signing certificate's name
            keychainPath = "..." // The path to the certificate's keychain
            notarize = true // Enables the notarization of the application third-party distribution
        }

        dmgDistribution { // .dmg distribution format options (optional)
            version = version(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's version for .dmg distribution (optional, overrides the version value in `packaging { }`)
            buildVersion = version(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's (build) version visible to the system for the .dmg distribution (optional, overrides the build version value in `packaging { }`)
        }

        pkgDistribution { // .pkg distribution format options (optional)
            version = version(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's version for .pkg distribution (optional, overrides the version value in `packaging { }`)
            buildVersion = version(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's (build) version visible to the system for the .pkg distribution (optional, overrides the build version value in `packaging { }`)
        }
    }
}
```

## Notes on the BuildModel API for `macosApplicatiom`

### MacosApplicationBuildModel (`macosApplication { ... }`)
Contains the low-level configuration of the (JVM) application:
1. The main class
2. A custom JAVA_HOME
3. Custom JPackage arguments
4. Custom JPackage JVM options
5. Proguard settings of the RELEASE build type
6. Specification of additional content with:
   - `provisioningProfile` file property
   - `runtimeProvisioningProfile` file property
7. Extra raw configuration for `Info.plist` file

### MacosSigningBuildModel (`signing { ... }`)
Contains the specification custom entitlements with the `entitlementsFile` and `runtimeEntitlementsFile`