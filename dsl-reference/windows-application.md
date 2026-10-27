# Windows Application DSL Reference

```kotlin
windowsApplication {
    distributions = listOf(msi, exe) // Limits the distribution formats for the application to packaged into on Windows platform, defaults to `msi` and `exe`

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
        version = version(<major-int>, <minor-int>, <build-int>) // The application's version
        copyright = "..." // The application's copyright information (optional)
        description = "..." // The description of the application (optional)
        vendor = "..." // The application's vendor (optional)
        license = layout.projectDirectory.file("...") // The application's license file (optional)

        icon = layout.projectDirectory.file("...") // The application's icon on Windows plaftform (.ico) (optional)
        shortcut = true // Enables creating a Windows shortcut upon installation (optional)
        runWithConsole = true // Enables running the application with the console (optional)
        chooseInstallDirectory = true // Enables the option for users to choose where to install the application (optional)
        perUserInstallation = true // Enables local installation of the application on the current Windows profile (optional)
        startMenuGroup = "..." // The group the application belongs to in Start menu (optional, defaults to null, i.e. the application will not be visible in the menu)

        msiDistribution { // .msi distribution format options (optional)
            version = version(<major-int>, <minor-int>, <build-int>) // The application's version for .msi distribution (optional, overrides the version value in `packaging { }`)
        }

        exeDistribution { // .exe distribution format options (optional)
            version = version(<major-int>, <minor-int>, <build-int>) // The application's version for .exe distribution (optional, overrides the version value in `packaging { }`)
        }
    }
}
```

## Notes on the BuildModel API for `windowsApplicatiom`

### WindowsApplicationBuildModel (`windowsApplication { ... }`)
Contains the low-level configuration of the (JVM) application:
1. The main class
2. A custom JAVA_HOME
3. Custom JPackage arguments
4. Custom JPackage JVM options
5. Proguard settings of the RELEASE build type
6. `upgradeUuid` associated with upgrades of this application; if a newer version of the package is installed, the old one is will be uninstalled; the ecosystem plugin provides a default