# Desktop Application DSL Reference

```kotlin
desktopApplication {
    // Specifies the OS platforms the application will be packaged/distributed for, defaults to empty list
    platforms = listOf(linux, macos, windows)
    
    kotlin { // Kotlin compilation configuration
        // Unfortunately, Compose configures and packages desktop applications to package the application in the following way:
        // 1. If it a JVM application (packaged with `jpackage`), it requires exactly ONE JVM target and uses its configuration for distribution to Linux, macOS, and Windows
        // 2. If it a Native application, it is packaged for all declarated macOS targets in the project. Native applications only support distribution to macOS.
        //    - Just as with libraries, where each subplatform shares the same configuration (compiler options etc.), we should do the same in case of `desktopApplication`
        //    - Currently, the only supported macOS compilation target in Kotlin/Native is `macosArm64`, so we can simplify the DSL by asserting that this would be the only target to compile and package the application to.
        // So ideally, we need exactly one JVM configuration and exactly one Native configuration to cover all cases.
        jvm { // Kotlin/JVM compilation configuration
            // Additional runtime modules for JVM application (optional)
            // By default, Compose includes these modules at runtime: "java.base", "java.desktop", "java.logging", "jdk.crypto.ec"
            additionalRuntimeModules = listOf("jdk.unsupported")
            compilerOptions { //  Compiler arguments to apply for all Kotlin JVM compilations
                // KotlinJvmCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-jvm-compiler-options/
            }
        }
        native { // Kotlin/Native compilation configuration
            compilerOptions { // Compiler arguments to apply for all Kotlin Native compilations
                // KotlinNativeCompilerOptions members are available here, see more at:
                // https://kotlinlang.org/api/kotlin-gradle-plugin/kotlin-gradle-plugin-api/org.jetbrains.kotlin.gradle.dsl/-kotlin-native-compiler-options/
            }
        }
    }

    linuxPlatform { // Linux platform options
        distributions = listOf(jvmDeb, jvmRpm) // Limits the distribution formats for the application to packaged into on Linux platform, defaults to `deb` and `rpm`
        shortcut = true // Enables creating a Linux shortcut upon installation
        category = "..." // The `Group` value in RPM .spec or `Section` value of DEB control file
        menuGroup = "..." // The menu group the application is placed in
    }

    macosPlatform { // macOS platform options
        // Limits the distribution formats for the application to packaged into on macOS platform, defaults to `jvmDmg`, `jvmPkg`, and `nativeDmg`
        // The reason for distinguishing between `jvm` and `native` distributions is mentioned above.
        distributions = listOf(jvmDmg, jvmPkg, nativeDmg)
        minimumSystemVersion = "..." // The minimum macOS version required to run the application
        // The category of the app in App Store (see more https://developer.apple.com/documentation/bundleresources/information-property-list/lsapplicationcategorytype), default is "public.app-category.utilities"
        // The property will be modelled as enum instead of raw strings in the format "public.app-category.<category>"
        category = utilities

        // NOTE: Signing is currently available for JVM applications only, in Native applications it is a no-op
        signing { // The signing options for macOS distribution
            enable = true // Enables signing for declared macOS distribution
            signForAppStore = true // Enables the building and signing of the application for Apple App Store
            identity = "..." // The signing certificate's name
            keychain = "..." // The path to the certificate's keychain
        }
    }

    windowsPlatform { // Windows platform options
        // Limits the distribution formats for the application to packaged into on Windows platform, defaults to `msi` and `exe`
        distributions = listOf(jvmMsi, jvmExe)
        shortcut = true // Enables creating a Windows shortcut upon installation
        runWithConsole = true // Enables running the application with the console
        chooseInstallDirectory = true // Enables the option for users to choose where to install the application
        perUserInstallation = true // Enables local installation of the application on the current Windows profile
        menuGroup = "..." // The group the application belongs to in Windows menu, defaults to null (the application will not be visible in the menu)
    }
    
    packaging { // Application packaging configuration
        name = "..." // The application's (package) name (optional, the default is the project name)
        version = "<any-format>" // The application's (package) version
        copyright = "..." // The application's copyright information (optional)
        description = "..." // Optional property for the description of the application (optional)
        vendor = "..." // The application's vendor (optional)
        license = layout.projectDirectory.file("...") // The application's license file (optional)
        
        linuxPlatform {
            version = "<any-format>" // The application's version on Linux platform (optional, overrides the version value in `packaging { }`)
            icon = layout.projectDirectory.file("...") // The application's icon on Linux plaftform (.png) (optional)
        }
        
        macosPlatform {
            version = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's version on macOS platform (optional, overrides the version value in `packaging { }`)
            buildVersion = macosVersion(<major-int>, <minor-int> (optional), <patch-int> (optional)) // The application's (build) version visible to the system (optional, defaults to the application's version)
            icon = layout.projectDirectory.file("...") // The application's icon on macOS plaftform (.icns) (optional)
        }
        
        windowsPlatform {
            version = windowsVersion(<major-int>, <minor-int>, <build-int>) // The application's version for Windows platform (optional, overrides the version value in `packaging { }`)
            icon = layout.projectDirectory.file("...") // The application's icon on Windows plaftform (.ico) (optional)
        }
    }
}
```

## Notes on DSL and build models
Changes from the original prototype:
- Moved OS distribution platforms to the top level block, compiler options for JVM and Native applications are contained in a `kotlin { ... }` block
  - Each platform has a `distributions` property that clearly names what type of application each distribution format is compiled on, either prefixed `jvm` or `native` for JVM and Native application, respectively.
- Distribution format-specific version were removed
- macOS app notarization options should not be exposed in the DSL for security reasons
- `upgradeUuid` property for Windows platform will receive a default by the ecosystem plugin (maybe store it in the `.properties` file)
For further remarks, see the [initial prototype](../desktop-application.md).